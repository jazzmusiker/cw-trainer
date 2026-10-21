package de.cwtrainer.app

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.random.Random
import kotlin.time.TimeMark
import kotlin.time.TimeSource

private enum class RequestedAction { None, Pause, Stop }

class TrainerController {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    private val _state = MutableStateFlow(readInitialState())
    val state: StateFlow<TrainerUiState> = _state.asStateFlow()

    private val requestedAction = MutableStateFlow(RequestedAction.None)
    private val appVisible = MutableStateFlow(false)
    private val audioConfigurationMutex = Mutex()
    private val resumeGate = MutableStateFlow<CompletableDeferred<Unit>?>(null)
    private var trainingJob: Job? = null
    private var previewJob: Job? = null
    private var audioPreparationJob: Job? = null
    private var timerJob: Job? = null
    private var segmentStart: TimeMark? = null
    private var segmentBaseMillis: Long = _state.value.remainingMillis

    private fun readInitialState(): TrainerUiState {
        val saved = runCatching { loadSavedProfiles()?.let { json.decodeFromString<SavedProfiles>(it) } }.getOrNull()
            ?: SavedProfiles()
        val profiles = saved.profiles.ifEmpty { listOf(TrainingProfile(id = "default", name = "Default")) }
        val selection = saved.selectedProfileId.takeIf { id -> profiles.any { it.id == id } } ?: profiles.first().id
        val trainingUnit = runCatching { loadSavedTrainingUnit() }.getOrNull()
            ?.let { savedValue -> TrainingUnit.values().firstOrNull { it.storageKey == savedValue } }
            ?: TrainingUnit.Reading
        val statistics = readSavedStatistics()
        val normalized = profiles.map { profile ->
            val safeIds = profile.enabledCharacterIds.filter { id -> MorseCharacters.all.any { it.id == id } }
            profile.copy(
                enabledCharacterIds = safeIds.ifEmpty { listOf(MorseCharacters.all.first().id) },
                speedWpm = profile.speedWpm.coerceIn(5, 30),
                wordLengthMin = profile.wordLengthMin.coerceIn(1, 10),
                wordLengthMax = profile.wordLengthMax.coerceIn(profile.wordLengthMin.coerceIn(1, 10), 10),
                trainingLengthSeconds = profile.trainingLengthSeconds.takeIf { it in listOf(30, 60, 120, 180, 300) } ?: 30,
                pauseBeforeStartSeconds = profile.pauseBeforeStartSeconds.coerceIn(0, 5),
                frequencyHz = profile.frequencyHz.coerceIn(100.0, 2_000.0),
                pauseBetweenCharacters = profile.pauseBetweenCharacters.coerceIn(3, 20),
                pauseBetweenGroups = profile.pauseBetweenGroups.coerceIn(7, 30),
            )
        }
        return TrainerUiState(
            profiles = normalized,
            selectedProfileId = selection,
            trainingUnit = trainingUnit,
            remainingMillis = normalized.first { it.id == selection }.trainingLengthSeconds * 1_000L,
            statistics = statistics,
        )
    }

    private fun readSavedStatistics(): SavedStatistics {
        val saved = runCatching {
            loadSavedStatistics()?.let { json.decodeFromString<SavedStatistics>(it) }
        }.getOrNull() ?: return SavedStatistics()
        val knownIds = MorseCharacters.all.mapTo(mutableSetOf()) { it.id }.apply {
            add(GroupSpaceStatisticId)
        }
        return SavedStatistics(
            totalPresentedItems = saved.totalPresentedItems.coerceAtLeast(0L),
            characters = saved.characters
                .filterKeys { it in knownIds }
                .mapValues { (_, counts) ->
                    counts.copy(
                        correctlyHeard = counts.correctlyHeard.coerceAtLeast(0L),
                        notHeard = counts.notHeard.coerceAtLeast(0L),
                        falselyHeard = counts.falselyHeard.coerceAtLeast(0L),
                    )
                },
        )
    }

    fun dispose() {
        timerJob?.cancel()
        trainingJob?.cancel()
        previewJob?.cancel()
        audioPreparationJob?.cancel()
        scope.cancel()
        closeAudioOutput()
    }

    fun onAppVisibilityChanged(isVisible: Boolean) {
        if (isVisible) {
            if (appVisible.value) return
            appVisible.value = true
            scheduleToneSamplePreparation(_state.value.selectedProfile)
            return
        }

        if (!appVisible.value) return
        appVisible.value = false
        audioPreparationJob?.cancel()
        previewJob?.cancel()
        if (_state.value.status in listOf(TrainingStatus.Starting, TrainingStatus.Playing, TrainingStatus.Paused)) {
            trainingJob?.cancel()
            finish(TrainingStatus.Stopped, "Training beim Wechsel in den Hintergrund beendet")
        }
        closeAudioOutput()
    }

    private suspend fun prepareToneSamples(profile: TrainingProfile) {
        audioConfigurationMutex.withLock {
            configureToneSamples(profile.frequencyHz, profile.speedWpm)
        }
    }

    private suspend fun beginAudioTransmission(profile: TrainingProfile) {
        audioConfigurationMutex.withLock {
            configureToneSamples(profile.frequencyHz, profile.speedWpm)
            openAudioOutput()
        }
    }

    private fun scheduleToneSamplePreparation(profile: TrainingProfile) {
        if (!appVisible.value) return
        audioPreparationJob?.cancel()
        audioPreparationJob = scope.launch {
            try {
                prepareToneSamples(profile)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (failure: Throwable) {
                _state.update { it.copy(message = "Audioausgabe nicht verfügbar: ${failure.message ?: "unbekannter Fehler"}") }
            }
        }
    }

    fun toggleScreen() {
        _state.update { it.copy(screen = if (it.screen == TrainerScreen.Training) TrainerScreen.Settings else TrainerScreen.Training) }
    }

    fun showStatistics() {
        _state.update { it.copy(screen = TrainerScreen.Statistics) }
    }

    fun selectTrainingUnit(trainingUnit: TrainingUnit) {
        val current = _state.value
        if (current.previewing || current.status in listOf(TrainingStatus.Starting, TrainingStatus.Playing, TrainingStatus.Paused)) return
        if (current.trainingUnit == trainingUnit) return
        runCatching { saveTrainingUnit(trainingUnit.storageKey) }
        _state.update {
            it.copy(
                trainingUnit = trainingUnit,
                status = TrainingStatus.Idle,
                remainingMillis = it.selectedProfile.trainingLengthSeconds * 1_000L,
                startDelayRemainingMillis = 0,
                transcript = "",
                visibleTranscript = null,
                transmittedCharacters = emptyList(),
                pendingReview = null,
                message = null,
            )
        }
    }

    fun selectProfile(profileId: String) {
        if (_state.value.previewing) return
        if (_state.value.status in listOf(TrainingStatus.Starting, TrainingStatus.Playing, TrainingStatus.Paused)) return
        if (_state.value.profiles.none { it.id == profileId }) return
        persist(_state.value.profiles, profileId)
        val profile = _state.value.profiles.first { it.id == profileId }
        _state.update {
            it.copy(
                selectedProfileId = profileId,
                status = TrainingStatus.Idle,
                remainingMillis = profile.trainingLengthSeconds * 1_000L,
                startDelayRemainingMillis = 0,
                transcript = "",
                visibleTranscript = null,
                message = null,
            )
        }
        scheduleToneSamplePreparation(profile)
    }

    fun addProfile(rawName: String): Boolean {
        if (_state.value.previewing) return false
        if (_state.value.status in listOf(TrainingStatus.Starting, TrainingStatus.Playing, TrainingStatus.Paused)) return false
        val name = rawName.trim()
        if (name.isEmpty() || _state.value.profiles.any { it.name.equals(name, ignoreCase = true) }) return false
        val id = "profile-${Random.nextLong().toULong().toString(16)}"
        val profile = TrainingProfile(id = id, name = name)
        val profiles = _state.value.profiles + profile
        persist(profiles, id)
        _state.update {
            it.copy(
                profiles = profiles,
                selectedProfileId = id,
                status = TrainingStatus.Idle,
                remainingMillis = profile.trainingLengthSeconds * 1_000L,
                startDelayRemainingMillis = 0,
                transcript = "",
                visibleTranscript = null,
                message = null,
            )
        }
        scheduleToneSamplePreparation(profile)
        return true
    }

    fun renameProfile(profileId: String, rawName: String): Boolean {
        val current = _state.value
        val name = rawName.trim()
        val target = current.profiles.firstOrNull { it.id == profileId } ?: return false
        if (name.isEmpty() || current.profiles.any { it.id != profileId && it.name.equals(name, ignoreCase = true) }) return false
        if (target.name == name) return true
        val profiles = current.profiles.map { if (it.id == profileId) it.copy(name = name) else it }
        persist(profiles, current.selectedProfileId)
        _state.update { it.copy(profiles = profiles) }
        return true
    }

    fun deleteProfile(profileId: String): Boolean {
        val current = _state.value
        if (current.previewing) return false
        if (current.status in listOf(TrainingStatus.Starting, TrainingStatus.Playing, TrainingStatus.Paused)) return false
        if (current.profiles.size <= 1 || current.profiles.none { it.id == profileId }) return false

        val profiles = current.profiles.filterNot { it.id == profileId }
        val deletingSelected = current.selectedProfileId == profileId
        val selectedId = if (deletingSelected) profiles.first().id else current.selectedProfileId
        persist(profiles, selectedId)
        _state.update {
            if (deletingSelected) {
                val nextProfile = profiles.first { it.id == selectedId }
                it.copy(
                    profiles = profiles,
                    selectedProfileId = selectedId,
                    status = TrainingStatus.Idle,
                    remainingMillis = nextProfile.trainingLengthSeconds * 1_000L,
                    startDelayRemainingMillis = 0,
                    transcript = "",
                    visibleTranscript = null,
                    message = null,
                )
            } else {
                it.copy(profiles = profiles)
            }
        }
        if (deletingSelected) scheduleToneSamplePreparation(_state.value.selectedProfile)
        return true
    }

    fun updateProfile(update: (TrainingProfile) -> TrainingProfile) {
        val current = _state.value
        val selected = current.selectedProfile
        val changed = update(selected).let { profile ->
            val ids = profile.enabledCharacterIds.distinct().filter { id -> MorseCharacters.all.any { it.id == id } }
            profile.copy(
                enabledCharacterIds = ids.ifEmpty { listOf(MorseCharacters.all.first().id) },
                speedWpm = profile.speedWpm.coerceIn(5, 30),
                wordLengthMin = profile.wordLengthMin.coerceIn(1, 10),
                wordLengthMax = profile.wordLengthMax.coerceIn(profile.wordLengthMin.coerceIn(1, 10), 10),
                trainingLengthSeconds = profile.trainingLengthSeconds.takeIf { it in listOf(30, 60, 120, 180, 300) } ?: 30,
                pauseBeforeStartSeconds = profile.pauseBeforeStartSeconds.coerceIn(0, 5),
                frequencyHz = profile.frequencyHz.coerceIn(100.0, 2_000.0),
                pauseBetweenCharacters = profile.pauseBetweenCharacters.coerceIn(3, 20),
                pauseBetweenGroups = profile.pauseBetweenGroups.coerceIn(7, 30),
            )
        }
        val profiles = current.profiles.map { if (it.id == selected.id) changed else it }
        val resetCharacterPractice = current.trainingUnit == TrainingUnit.CharacterPractice && changed != selected
        if (resetCharacterPractice) requestedAction.value = RequestedAction.None
        persist(profiles, current.selectedProfileId)
        _state.update {
            it.copy(
                profiles = profiles,
                remainingMillis = if (it.status == TrainingStatus.Idle) changed.trainingLengthSeconds * 1_000L else it.remainingMillis,
                status = if (resetCharacterPractice) TrainingStatus.Idle else it.status,
                startDelayRemainingMillis = if (resetCharacterPractice) 0 else it.startDelayRemainingMillis,
                transcript = if (resetCharacterPractice) "" else it.transcript,
                visibleTranscript = if (resetCharacterPractice) null else it.visibleTranscript,
                transmittedCharacters = if (resetCharacterPractice) emptyList() else it.transmittedCharacters,
                pendingReview = if (resetCharacterPractice) null else it.pendingReview,
                characterPracticeSession = if (resetCharacterPractice) null else it.characterPracticeSession,
                message = if (resetCharacterPractice) null else it.message,
            )
        }
        if (resetCharacterPractice && current.status in listOf(TrainingStatus.Starting, TrainingStatus.Playing, TrainingStatus.Paused)) {
            closeAudioOutput()
            trainingJob?.cancel()
            trainingJob = null
        }
        val audioSettingsChanged = changed.frequencyHz != selected.frequencyHz || changed.speedWpm != selected.speedWpm
        val trainingActive = current.status in listOf(TrainingStatus.Starting, TrainingStatus.Playing, TrainingStatus.Paused)
        if (audioSettingsChanged && !trainingActive && !current.previewing) scheduleToneSamplePreparation(changed)
    }

    fun previewCqTest() {
        val current = _state.value
        if (current.previewing) return
        if (current.status in listOf(TrainingStatus.Starting, TrainingStatus.Playing, TrainingStatus.Paused)) return
        if (previewJob?.isActive == true) return

        val profile = current.selectedProfile
        val dotMillis = dotDurationMillis(profile.speedWpm)
        val cq = listOf("c", "q").map { id -> MorseCharacters.all.first { it.id == id } }
        val test = listOf("t", "e", "s", "t").map { id -> MorseCharacters.all.first { it.id == id } }
        _state.update { it.copy(previewing = true) }
        previewJob = scope.launch {
            try {
                beginAudioTransmission(profile)
                emitPreviewWord(cq, profile, dotMillis)
                delay(profile.pauseBetweenGroups * dotMillis)
                emitPreviewWord(test, profile, dotMillis)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (failure: Throwable) {
                _state.update { it.copy(message = "Audioausgabe nicht verfügbar: ${failure.message ?: "unbekannter Fehler"}") }
            } finally {
                closeAudioOutput()
                _state.update { it.copy(previewing = false) }
                previewJob = null
                scheduleToneSamplePreparation(_state.value.selectedProfile)
            }
        }
    }

    private suspend fun emitPreviewWord(word: List<MorseCharacter>, profile: TrainingProfile, dotMillis: Long) {
        word.forEachIndexed { index, character ->
            if (index > 0) delay(profile.pauseBetweenCharacters * dotMillis)
            emitCharacter(character)
        }
    }

    private fun persist(profiles: List<TrainingProfile>, selectedId: String) {
        runCatching { saveProfiles(json.encodeToString(SavedProfiles(selectedId, profiles))) }
    }

    fun markReviewAsNotHeard(index: Int) {
        updateReviewEntry(index) { it.copy(heardCharacterId = null) }
    }

    fun markReviewAsHeard(index: Int, heardCharacterId: String) {
        if (heardCharacterId != GroupSpaceStatisticId && MorseCharacters.all.none { it.id == heardCharacterId }) return
        updateReviewEntry(index) { it.copy(heardCharacterId = heardCharacterId) }
    }

    fun discardReview() {
        _state.update {
            it.copy(
                pendingReview = null,
                transmittedCharacters = emptyList(),
                visibleTranscript = it.transcript,
                message = null,
            )
        }
    }

    fun answerCharacterPractice(heardCharacterId: String) {
        _state.update { current ->
            val session = current.characterPracticeSession ?: return@update current
            if (current.trainingUnit != TrainingUnit.CharacterPractice || current.status != TrainingStatus.Playing || session.finished) return@update current
            if (heardCharacterId !in session.poolCharacterIds) return@update current
            val emittedCharacterId = session.pendingCharacterId ?: return@update current
            val counts = session.counts.toMutableMap()
            if (heardCharacterId == emittedCharacterId) {
                counts[emittedCharacterId] = (counts[emittedCharacterId] ?: CharacterStatistics()).let {
                    it.copy(correctlyHeard = it.correctlyHeard + 1L)
                }
            } else {
                counts[emittedCharacterId] = (counts[emittedCharacterId] ?: CharacterStatistics()).let {
                    it.copy(notHeard = it.notHeard + 1L)
                }
                counts[heardCharacterId] = (counts[heardCharacterId] ?: CharacterStatistics()).let {
                    it.copy(falselyHeard = it.falselyHeard + 1L)
                }
            }
            current.copy(
                characterPracticeSession = session.copy(
                    counts = counts,
                    pendingCharacterId = null,
                ),
            )
        }
    }

    fun commitCharacterPracticeStatistics() {
        val current = _state.value
        val session = current.characterPracticeSession ?: return
        if (!session.finished || session.resolved) return

        val counts = current.statistics.characters.toMutableMap()
        session.counts.forEach { (characterId, practiceCounts) ->
            val savedCounts = counts[characterId] ?: CharacterStatistics()
            counts[characterId] = savedCounts.copy(
                correctlyHeard = savedCounts.correctlyHeard + practiceCounts.correctlyHeard,
                notHeard = savedCounts.notHeard + practiceCounts.notHeard,
                falselyHeard = savedCounts.falselyHeard + practiceCounts.falselyHeard,
            )
        }
        val updatedStatistics = SavedStatistics(
            totalPresentedItems = current.statistics.totalPresentedItems + session.presentedCount,
            characters = counts,
        )
        val saved = runCatching { saveStatistics(json.encodeToString(updatedStatistics)) }.isSuccess
        if (!saved) {
            _state.update { it.copy(message = "Statistik konnte nicht gespeichert werden") }
            return
        }
        _state.update {
            if (it.characterPracticeSession == session) {
                it.copy(
                    statistics = updatedStatistics,
                    characterPracticeSession = session.copy(resolved = true),
                    message = null,
                )
            } else it
        }
    }

    fun discardCharacterPracticeStatistics() {
        _state.update { current ->
            val session = current.characterPracticeSession ?: return@update current
            if (!session.finished || session.resolved) return@update current
            current.copy(characterPracticeSession = session.copy(resolved = true), message = null)
        }
    }

    private fun updateReviewEntry(index: Int, update: (TrainingReviewEntry) -> TrainingReviewEntry) {
        _state.update { current ->
            val review = current.pendingReview ?: return@update current
            if (index !in review.indices) return@update current
            current.copy(pendingReview = review.toMutableList().also { it[index] = update(it[index]) })
        }
    }

    fun commitReview() {
        val current = _state.value
        val review = current.pendingReview ?: return
        val counts = current.statistics.characters.toMutableMap()
        fun changeCount(characterId: String, change: (CharacterStatistics) -> CharacterStatistics) {
            counts[characterId] = change(counts[characterId] ?: CharacterStatistics())
        }

        review.forEach { entry ->
            when (val heardId = entry.heardCharacterId) {
                entry.emittedCharacterId -> changeCount(entry.emittedCharacterId) {
                    it.copy(correctlyHeard = it.correctlyHeard + 1L)
                }
                null -> changeCount(entry.emittedCharacterId) {
                    it.copy(notHeard = it.notHeard + 1L)
                }
                else -> {
                    changeCount(entry.emittedCharacterId) { it.copy(notHeard = it.notHeard + 1L) }
                    changeCount(heardId) { it.copy(falselyHeard = it.falselyHeard + 1L) }
                }
            }
        }

        val updatedStatistics = SavedStatistics(
            totalPresentedItems = current.statistics.totalPresentedItems + review.size,
            characters = counts,
        )
        val saved = runCatching {
            saveStatistics(json.encodeToString(updatedStatistics))
        }.isSuccess
        if (!saved) {
            _state.update { it.copy(message = "Statistik konnte nicht gespeichert werden") }
            return
        }
        _state.update {
            if (it.pendingReview == review) it.copy(statistics = updatedStatistics, pendingReview = null, message = null)
            else it
        }
    }

    fun resetStatistics() {
        val emptyStatistics = SavedStatistics()
        val saved = runCatching { saveStatistics(json.encodeToString(emptyStatistics)) }.isSuccess
        if (!saved) {
            _state.update { it.copy(message = "Statistik konnte nicht zurückgesetzt werden") }
            return
        }
        _state.update { current ->
            val activeCharacterSession = current.characterPracticeSession
                ?.takeIf { current.trainingUnit == TrainingUnit.CharacterPractice && current.status in listOf(TrainingStatus.Starting, TrainingStatus.Playing) }
                ?.let { session ->
                    session.copy(
                        counts = session.poolCharacterIds.associateWith { CharacterStatistics() },
                        presentedCount = 0,
                        pendingCharacterId = null,
                    )
                }
            current.copy(
                statistics = emptyStatistics,
                pendingReview = null,
                characterPracticeSession = activeCharacterSession,
                transcript = "",
                visibleTranscript = null,
                transmittedCharacters = emptyList(),
                message = null,
            )
        }
    }

    fun startOrResume() {
        if (_state.value.previewing || previewJob?.isActive == true) return
        if (_state.value.trainingUnit == TrainingUnit.CharacterPractice) {
            if (_state.value.status in listOf(TrainingStatus.Starting, TrainingStatus.Playing)) return
            startCharacterPractice()
            return
        }
        when (_state.value.status) {
            TrainingStatus.Paused -> {
                requestedAction.value = RequestedAction.None
                resumeGate.value?.complete(Unit)
            }
            TrainingStatus.Starting, TrainingStatus.Playing -> Unit
            else -> startNewTraining()
        }
    }

    private fun startNewTraining() {
        val profile = _state.value.selectedProfile
        val enabled = profile.enabledCharacterIds.mapNotNull { id -> MorseCharacters.all.firstOrNull { it.id == id } }
            .ifEmpty { listOf(MorseCharacters.all.first()) }
        val durationMillis = profile.trainingLengthSeconds * 1_000L
        val startDelayMillis = profile.pauseBeforeStartSeconds * 1_000L
        requestedAction.value = RequestedAction.None
        resumeGate.value = null
        timerJob?.cancel()
        timerJob = null
        segmentStart = null
        segmentBaseMillis = durationMillis
        _state.update {
            it.copy(
                status = if (startDelayMillis > 0L) TrainingStatus.Starting else TrainingStatus.Playing,
                remainingMillis = durationMillis,
                startDelayRemainingMillis = startDelayMillis,
                transcript = "",
                visibleTranscript = null,
                transmittedCharacters = emptyList(),
                pendingReview = null,
                characterPracticeSession = null,
                message = if (startDelayMillis > 0L) "Morsen startet" else "Training läuft",
            )
        }
        trainingJob = scope.launch {
            try {
                prepareToneSamples(profile)
                if (startDelayMillis > 0L) {
                    val startMark = TimeSource.Monotonic.markNow()
                    while (true) {
                        val left = (startDelayMillis - startMark.elapsedNow().inWholeMilliseconds).coerceAtLeast(0L)
                        _state.update {
                            it.copy(
                                startDelayRemainingMillis = left,
                                message = if (left > 0L) "Morsen startet in ${(left + 999L) / 1_000L} s" else "Morsen startet",
                            )
                        }
                        if (requestedAction.value == RequestedAction.Stop) {
                            finish(TrainingStatus.Stopped, null)
                            return@launch
                        }
                        if (left <= 0L) break
                        delay(minOf(left, 100L))
                    }
                }
                if (requestedAction.value == RequestedAction.Stop) {
                    finish(TrainingStatus.Stopped, null)
                    return@launch
                }
                beginAudioTransmission(profile)
                _state.update {
                    it.copy(
                        status = TrainingStatus.Playing,
                        startDelayRemainingMillis = 0,
                        message = "Training läuft",
                    )
                }
                startTimerSegment(durationMillis)
                runSession(profile, enabled)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (failure: Throwable) {
                finish(TrainingStatus.Stopped, "Audioausgabe nicht verfügbar: ${failure.message ?: "unbekannter Fehler"}")
            }
        }
    }

    private fun startCharacterPractice() {
        val current = _state.value
        val profile = current.selectedProfile
        val enabledIds = profile.enabledCharacterIds.toSet()
        val pool = MorseCharacters.all.filter { it.id in enabledIds }.take(8)
            .ifEmpty { listOf(MorseCharacters.all.first()) }
        val startDelayMillis = profile.pauseBeforeStartSeconds * 1_000L
        requestedAction.value = RequestedAction.None
        resumeGate.value = null
        timerJob?.cancel()
        timerJob = null
        segmentStart = null
        segmentBaseMillis = 0L
        val session = CharacterPracticeSession(
            poolCharacterIds = pool.map { it.id },
            counts = pool.associate { it.id to CharacterStatistics() },
            speedWpm = profile.speedWpm,
        )
        _state.update {
            it.copy(
                status = if (startDelayMillis > 0L) TrainingStatus.Starting else TrainingStatus.Playing,
                remainingMillis = 0L,
                startDelayRemainingMillis = startDelayMillis,
                transcript = "",
                visibleTranscript = null,
                transmittedCharacters = emptyList(),
                pendingReview = null,
                characterPracticeSession = session,
                message = if (startDelayMillis > 0L) "Morsen startet" else "Training läuft",
            )
        }
        trainingJob = scope.launch {
            try {
                prepareToneSamples(profile)
                if (startDelayMillis > 0L) {
                    val startMark = TimeSource.Monotonic.markNow()
                    while (true) {
                        val left = (startDelayMillis - startMark.elapsedNow().inWholeMilliseconds).coerceAtLeast(0L)
                        _state.update {
                            it.copy(
                                startDelayRemainingMillis = left,
                                message = if (left > 0L) "Morsen startet in ${(left + 999L) / 1_000L} s" else "Morsen startet",
                            )
                        }
                        if (requestedAction.value == RequestedAction.Stop) {
                            finishCharacterPractice(TrainingStatus.Stopped, null)
                            return@launch
                        }
                        if (left <= 0L) break
                        delay(minOf(left, 100L))
                    }
                }
                if (requestedAction.value == RequestedAction.Stop) {
                    finishCharacterPractice(TrainingStatus.Stopped, null)
                    return@launch
                }
                beginAudioTransmission(profile)
                _state.update {
                    it.copy(
                        status = TrainingStatus.Playing,
                        startDelayRemainingMillis = 0,
                        message = "Training läuft",
                    )
                }
                runCharacterPractice(profile, pool)
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                if (requestedAction.value == RequestedAction.Stop) {
                    finishCharacterPractice(TrainingStatus.Stopped, null)
                } else {
                    throw cancelled
                }
            } catch (failure: Throwable) {
                if (requestedAction.value == RequestedAction.Stop) {
                    finishCharacterPractice(TrainingStatus.Stopped, null)
                } else {
                    finishCharacterPractice(
                        TrainingStatus.Stopped,
                        "Audioausgabe nicht verfügbar: ${failure.message ?: "unbekannter Fehler"}",
                    )
                }
            }
        }
    }

    private suspend fun runCharacterPractice(profile: TrainingProfile, pool: List<MorseCharacter>) {
        val characterPauseMillis = profile.pauseBetweenCharacters * dotDurationMillis(profile.speedWpm)
        while (true) {
            if (requestedAction.value == RequestedAction.Stop) {
                finishCharacterPractice(TrainingStatus.Stopped, null)
                return
            }
            val character = pool.random()
            playMorsePattern(character.pattern)
            completeCharacterPracticeCharacter(character.id)

            var remainingPause = characterPauseMillis
            while (remainingPause > 0L) {
                if (requestedAction.value == RequestedAction.Stop) {
                    finishCharacterPractice(TrainingStatus.Stopped, null)
                    return
                }
                val pauseSlice = minOf(remainingPause, 40L)
                delay(pauseSlice)
                remainingPause -= pauseSlice
            }
        }
    }

    private fun completeCharacterPracticeCharacter(characterId: String) {
        _state.update { current ->
            val session = current.characterPracticeSession ?: return@update current
            if (session.finished) return@update current
            val counts = session.counts.toMutableMap()
            session.pendingCharacterId?.let { unansweredId ->
                counts[unansweredId] = (counts[unansweredId] ?: CharacterStatistics()).let {
                    it.copy(notHeard = it.notHeard + 1L)
                }
            }
            current.copy(
                characterPracticeSession = session.copy(
                    counts = counts,
                    presentedCount = session.presentedCount + 1L,
                    pendingCharacterId = characterId,
                ),
            )
        }
    }

    fun requestPause() {
        if (_state.value.status == TrainingStatus.Playing) requestedAction.value = RequestedAction.Pause
    }

    fun requestStop() {
        if (_state.value.trainingUnit == TrainingUnit.CharacterPractice) {
            when (_state.value.status) {
                TrainingStatus.Starting -> requestedAction.value = RequestedAction.Stop
                TrainingStatus.Playing -> {
                    requestedAction.value = RequestedAction.Stop
                    closeAudioOutput()
                    trainingJob?.cancel()
                }
                else -> Unit
            }
            return
        }
        when (_state.value.status) {
            TrainingStatus.Starting, TrainingStatus.Playing -> requestedAction.value = RequestedAction.Stop
            TrainingStatus.Paused -> {
                requestedAction.value = RequestedAction.Stop
                resumeGate.value?.complete(Unit)
            }
            TrainingStatus.Stopped, TrainingStatus.Finished -> {
                _state.update { it.copy(visibleTranscript = it.transcript, message = null) }
            }
            TrainingStatus.Idle -> Unit
        }
    }

    private suspend fun runSession(profile: TrainingProfile, enabled: List<MorseCharacter>) {
        val dotMillis = dotDurationMillis(profile.speedWpm)
        var firstGroup = true
        try {
            while (true) {
                if (!firstGroup) {
                    if (!controlledGap(profile.pauseBetweenGroups * dotMillis)) return
                    appendGroupSeparator()
                }
                firstGroup = false
                val groupSize = Random.nextInt(profile.wordLengthMin, profile.wordLengthMax + 1)
                repeat(groupSize) { index ->
                    if (index > 0 && !controlledGap(profile.pauseBetweenCharacters * dotMillis)) return
                    if (remainingNow() <= 0L) {
                        finish(TrainingStatus.Finished, "Training beendet")
                        return
                    }
                    val character = enabled.random()
                    emitCharacter(character)
                    appendTranscript(character, index == 0 && _state.value.transcript.isNotEmpty())
                    if (!handleCharacterBoundary()) return
                }
            }
        } catch (cancelled: kotlinx.coroutines.CancellationException) {
            throw cancelled
        } catch (failure: Throwable) {
            finish(TrainingStatus.Stopped, "Audioausgabe nicht verfügbar: ${failure.message ?: "unbekannter Fehler"}")
        }
    }

    private suspend fun emitCharacter(character: MorseCharacter) {
        playMorsePattern(character.pattern)
    }

    private fun appendTranscript(character: MorseCharacter, startsGroup: Boolean) {
        _state.update { current ->
            current.copy(
                transcript = current.transcript + character.label,
                transmittedCharacters = current.transmittedCharacters + TrainingReviewEntry(
                    emittedCharacterId = character.id,
                    startsGroup = startsGroup,
                ),
            )
        }
    }

    private fun appendGroupSeparator() {
        _state.update { current ->
            current.copy(
                transcript = current.transcript + "  ",
                transmittedCharacters = current.transmittedCharacters + TrainingReviewEntry(
                    emittedCharacterId = GroupSpaceStatisticId,
                    startsGroup = false,
                ),
            )
        }
    }

    private suspend fun controlledGap(durationMillis: Long): Boolean {
        var remainingGap = durationMillis
        while (remainingGap > 0) {
            if (!handleCharacterBoundary()) return false
            val slice = minOf(remainingGap, 50L)
            delay(slice)
            remainingGap -= slice
        }
        return handleCharacterBoundary()
    }

    private suspend fun handleCharacterBoundary(): Boolean {
        val remaining = remainingNow()
        _state.update { it.copy(remainingMillis = remaining) }
        if (remaining <= 0L) {
            finish(TrainingStatus.Finished, "Training beendet")
            return false
        }
        when (requestedAction.value) {
            RequestedAction.None -> return true
            RequestedAction.Stop -> {
                finish(TrainingStatus.Stopped, null)
                return false
            }
            RequestedAction.Pause -> {
                closeAudioOutput()
                freezeTimer()
                requestedAction.value = RequestedAction.None
                val gate = CompletableDeferred<Unit>()
                resumeGate.value = gate
                _state.update { it.copy(status = TrainingStatus.Paused, message = "Training pausiert") }
                gate.await()
                resumeGate.value = null
                if (requestedAction.value == RequestedAction.Stop) {
                    finish(TrainingStatus.Stopped, null)
                    return false
                }
                requestedAction.value = RequestedAction.None
                val resumedWith = _state.value.remainingMillis
                if (resumedWith <= 0L) {
                    finish(TrainingStatus.Finished, "Training beendet")
                    return false
                }
                openAudioOutput()
                startTimerSegment(resumedWith)
                _state.update { it.copy(status = TrainingStatus.Playing, message = "Training läuft") }
                return true
            }
        }
    }

    private fun startTimerSegment(remainingMillis: Long) {
        timerJob?.cancel()
        segmentBaseMillis = remainingMillis
        segmentStart = TimeSource.Monotonic.markNow()
        timerJob = scope.launch {
            while (true) {
                delay(200)
                val remaining = remainingNow()
                _state.update { it.copy(remainingMillis = remaining) }
            }
        }
    }

    private fun remainingNow(): Long {
        val mark = segmentStart ?: return segmentBaseMillis
        return (segmentBaseMillis - mark.elapsedNow().inWholeMilliseconds).coerceAtLeast(0L)
    }

    private fun freezeTimer(): Long {
        val remaining = remainingNow()
        timerJob?.cancel()
        timerJob = null
        segmentStart = null
        segmentBaseMillis = remaining
        _state.update { it.copy(remainingMillis = remaining) }
        return remaining
    }

    private fun finish(status: TrainingStatus, message: String?) {
        if (_state.value.trainingUnit == TrainingUnit.CharacterPractice) {
            finishCharacterPractice(status, message)
            return
        }
        freezeTimer()
        closeAudioOutput()
        requestedAction.value = RequestedAction.None
        resumeGate.value?.complete(Unit)
        resumeGate.value = null
        _state.update {
            it.copy(
                status = status,
                startDelayRemainingMillis = 0,
                visibleTranscript = it.transcript,
                pendingReview = it.transmittedCharacters.takeIf { characters -> characters.isNotEmpty() },
                message = message,
            )
        }
        trainingJob = null
        if (appVisible.value) scheduleToneSamplePreparation(_state.value.selectedProfile)
    }

    private fun finishCharacterPractice(status: TrainingStatus, message: String?) {
        closeAudioOutput()
        requestedAction.value = RequestedAction.None
        resumeGate.value?.complete(Unit)
        resumeGate.value = null
        _state.update { current ->
            val session = current.characterPracticeSession
            val finishedSession = if (session == null || session.finished) {
                session
            } else {
                val counts = session.counts.toMutableMap()
                session.pendingCharacterId?.let { unansweredId ->
                    counts[unansweredId] = (counts[unansweredId] ?: CharacterStatistics()).let {
                        it.copy(notHeard = it.notHeard + 1L)
                    }
                }
                session.copy(
                    counts = counts,
                    pendingCharacterId = null,
                    finished = true,
                )
            }
            current.copy(
                status = status,
                startDelayRemainingMillis = 0,
                transcript = "",
                visibleTranscript = null,
                transmittedCharacters = emptyList(),
                pendingReview = null,
                characterPracticeSession = finishedSession,
                message = message,
            )
        }
        trainingJob = null
        if (appVisible.value) scheduleToneSamplePreparation(_state.value.selectedProfile)
    }
}
