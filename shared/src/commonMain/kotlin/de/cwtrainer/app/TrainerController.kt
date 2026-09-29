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
    private val resumeGate = MutableStateFlow<CompletableDeferred<Unit>?>(null)
    private var trainingJob: Job? = null
    private var timerJob: Job? = null
    private var segmentStart: TimeMark? = null
    private var segmentBaseMillis: Long = _state.value.remainingMillis

    private fun readInitialState(): TrainerUiState {
        val saved = runCatching { loadSavedProfiles()?.let { json.decodeFromString<SavedProfiles>(it) } }.getOrNull()
            ?: SavedProfiles()
        val profiles = saved.profiles.ifEmpty { listOf(TrainingProfile(id = "default", name = "Default")) }
        val selection = saved.selectedProfileId.takeIf { id -> profiles.any { it.id == id } } ?: profiles.first().id
        val normalized = profiles.map { profile ->
            val safeIds = profile.enabledCharacterIds.filter { id -> MorseCharacters.all.any { it.id == id } }
            profile.copy(
                enabledCharacterIds = safeIds.ifEmpty { listOf(MorseCharacters.all.first().id) },
                speedWpm = profile.speedWpm.coerceIn(5, 30),
                wordLengthMin = profile.wordLengthMin.coerceIn(1, 10),
                wordLengthMax = profile.wordLengthMax.coerceIn(profile.wordLengthMin.coerceIn(1, 10), 10),
                trainingLengthSeconds = profile.trainingLengthSeconds.takeIf { it in listOf(30, 60, 120, 180, 300) } ?: 30,
                frequencyHz = profile.frequencyHz.coerceIn(100.0, 2_000.0),
                pauseBetweenCharacters = profile.pauseBetweenCharacters.coerceIn(3, 20),
                pauseBetweenGroups = profile.pauseBetweenGroups.coerceIn(7, 30),
            )
        }
        return TrainerUiState(
            profiles = normalized,
            selectedProfileId = selection,
            remainingMillis = normalized.first { it.id == selection }.trainingLengthSeconds * 1_000L,
        )
    }

    fun dispose() {
        timerJob?.cancel()
        trainingJob?.cancel()
        scope.cancel()
    }

    fun toggleScreen() {
        _state.update { it.copy(screen = if (it.screen == TrainerScreen.Training) TrainerScreen.Settings else TrainerScreen.Training) }
    }

    fun selectProfile(profileId: String) {
        if (_state.value.profiles.none { it.id == profileId }) return
        persist(_state.value.profiles, profileId)
        val profile = _state.value.profiles.first { it.id == profileId }
        _state.update {
            it.copy(
                selectedProfileId = profileId,
                remainingMillis = if (it.status == TrainingStatus.Idle) profile.trainingLengthSeconds * 1_000L else it.remainingMillis,
            )
        }
    }

    fun addProfile(rawName: String): Boolean {
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
                remainingMillis = profile.trainingLengthSeconds * 1_000L,
            )
        }
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
                frequencyHz = profile.frequencyHz.coerceIn(100.0, 2_000.0),
                pauseBetweenCharacters = profile.pauseBetweenCharacters.coerceIn(3, 20),
                pauseBetweenGroups = profile.pauseBetweenGroups.coerceIn(7, 30),
            )
        }
        val profiles = current.profiles.map { if (it.id == selected.id) changed else it }
        persist(profiles, current.selectedProfileId)
        _state.update {
            it.copy(
                profiles = profiles,
                remainingMillis = if (it.status == TrainingStatus.Idle) changed.trainingLengthSeconds * 1_000L else it.remainingMillis,
            )
        }
    }

    private fun persist(profiles: List<TrainingProfile>, selectedId: String) {
        runCatching { saveProfiles(json.encodeToString(SavedProfiles(selectedId, profiles))) }
    }

    fun startOrResume() {
        when (_state.value.status) {
            TrainingStatus.Paused -> {
                requestedAction.value = RequestedAction.None
                resumeGate.value?.complete(Unit)
            }
            TrainingStatus.Playing -> Unit
            else -> startNewTraining()
        }
    }

    private fun startNewTraining() {
        val profile = _state.value.selectedProfile
        val enabled = profile.enabledCharacterIds.mapNotNull { id -> MorseCharacters.all.firstOrNull { it.id == id } }
            .ifEmpty { listOf(MorseCharacters.all.first()) }
        val durationMillis = profile.trainingLengthSeconds * 1_000L
        requestedAction.value = RequestedAction.None
        resumeGate.value = null
        _state.update {
            it.copy(
                status = TrainingStatus.Playing,
                remainingMillis = durationMillis,
                transcript = "",
                visibleTranscript = null,
                message = "Training läuft",
            )
        }
        startTimerSegment(durationMillis)
        trainingJob = scope.launch {
            runSession(profile, enabled)
        }
    }

    fun requestPause() {
        if (_state.value.status == TrainingStatus.Playing) requestedAction.value = RequestedAction.Pause
    }

    fun requestStop() {
        when (_state.value.status) {
            TrainingStatus.Playing -> requestedAction.value = RequestedAction.Stop
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
        val dotMillis = (1_200.0 / profile.speedWpm).toLong().coerceAtLeast(1L)
        var firstGroup = true
        try {
            while (true) {
                if (!firstGroup) {
                    if (!controlledGap(profile.pauseBetweenGroups * dotMillis)) return
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
                    emitCharacter(character, profile.frequencyHz, dotMillis)
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

    private suspend fun emitCharacter(character: MorseCharacter, frequencyHz: Double, dotMillis: Long) {
        character.pattern.forEachIndexed { index, mark ->
            playTone(frequencyHz, if (mark == '-') dotMillis * 3 else dotMillis)
            if (index < character.pattern.lastIndex) delay(dotMillis)
        }
    }

    private fun appendTranscript(character: MorseCharacter, startsGroup: Boolean) {
        _state.update { current ->
            val next = current.transcript + (if (startsGroup) " " else "") + character.label
            current.copy(transcript = next)
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
        freezeTimer()
        requestedAction.value = RequestedAction.None
        resumeGate.value?.complete(Unit)
        resumeGate.value = null
        _state.update {
            it.copy(
                status = status,
                visibleTranscript = it.transcript,
                message = message,
            )
        }
        trainingJob = null
    }
}
