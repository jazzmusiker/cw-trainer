package de.cwtrainer.app

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class TrainingProfile(
    val id: String,
    val name: String,
    val enabledCharacterIds: List<String> = MorseCharacters.all.map { it.id },
    val speedWpm: Int = 12,
    val wordLengthMin: Int = 1,
    val wordLengthMax: Int = 5,
    val trainingLengthSeconds: Int = 30,
    val pauseBeforeStartSeconds: Int = 0,
    val frequencyHz: Double = 600.0,
    val pauseBetweenCharacters: Int = 3,
    val pauseBetweenGroups: Int = 7,
)

@Serializable
data class SavedProfiles(
    val selectedProfileId: String = "default",
    val profiles: List<TrainingProfile> = listOf(TrainingProfile(id = "default", name = "Default")),
)

@Serializable
data class CharacterStatistics(
    val correctlyHeard: Long = 0,
    val notHeard: Long = 0,
    val falselyHeard: Long = 0,
)

@Serializable
data class SavedStatistics(
    @SerialName("totalPresentedCharacters")
    val totalPresentedItems: Long = 0,
    val characters: Map<String, CharacterStatistics> = emptyMap(),
)

const val GroupSpaceStatisticId = "__group_space__"
val GroupSpaceCharacter = MorseCharacter(GroupSpaceStatisticId, "␠", "Gruppenabstand")

data class TrainingReviewEntry(
    val emittedCharacterId: String,
    val startsGroup: Boolean,
    val heardCharacterId: String? = emittedCharacterId,
)

data class MorseCharacter(
    val id: String,
    val label: String,
    val pattern: String,
    val prosign: Boolean = false,
)

object MorseCharacters {
    private val letters = listOf(
        "a" to ".-", "b" to "-...", "c" to "-.-.", "d" to "-..", "e" to ".",
        "f" to "..-.", "g" to "--.", "h" to "....", "i" to "..", "j" to ".---",
        "k" to "-.-", "l" to ".-..", "m" to "--", "n" to "-.", "o" to "---",
        "p" to ".--.", "q" to "--.-", "r" to ".-.", "s" to "...", "t" to "-",
        "u" to "..-", "v" to "...-", "w" to ".--", "x" to "-..-", "y" to "-.--", "z" to "--..",
    )
    private val digits = listOf(
        "0" to "-----", "1" to ".----", "2" to "..---", "3" to "...--", "4" to "....-",
        "5" to ".....", "6" to "-....", "7" to "--...", "8" to "---..", "9" to "----.",
    )
    private val punctuation = listOf(
        "." to ".-.-.-", "," to "--..--", "-" to "-....-", ":" to "---...", "/" to "-..-.",
        "=" to "-...-", "?" to "..--..", "!" to "-.-.--", ";" to "-.-.-.", "(" to "-.--.", ")" to "-.--.-",
    )

    val all: List<MorseCharacter> = buildList {
        letters.forEach { (label, code) -> add(MorseCharacter(label, label, code)) }
        digits.forEach { (label, code) -> add(MorseCharacter(label, label, code)) }
        add(MorseCharacter("ä", "ä", ".-.-"))
        add(MorseCharacter("ö", "ö", "---."))
        add(MorseCharacter("ü", "ü", "..--"))
        punctuation.forEach { (label, code) -> add(MorseCharacter(label, label, code)) }
        add(MorseCharacter("<KA>", "<KA>", "-.-.-", prosign = true))
        add(MorseCharacter("<SK>", "<SK>", "...-.-", prosign = true))
        add(MorseCharacter("<AR>", "<AR>", ".-.-.", prosign = true))
        add(MorseCharacter("<BT>", "<BT>", "-...-", prosign = true))
        add(MorseCharacter("<KN>", "<KN>", "-.--.", prosign = true))
        add(MorseCharacter("<HH>", "<HH>", "........", prosign = true))
    }
}

enum class TrainerScreen { Training, Settings, Statistics }
enum class TrainingStatus { Idle, Starting, Playing, Paused, Stopped, Finished }

data class TrainerUiState(
    val profiles: List<TrainingProfile>,
    val selectedProfileId: String,
    val screen: TrainerScreen = TrainerScreen.Training,
    val status: TrainingStatus = TrainingStatus.Idle,
    val remainingMillis: Long = 30_000,
    val startDelayRemainingMillis: Long = 0,
    val previewing: Boolean = false,
    val transcript: String = "",
    val visibleTranscript: String? = null,
    val transmittedCharacters: List<TrainingReviewEntry> = emptyList(),
    val pendingReview: List<TrainingReviewEntry>? = null,
    val statistics: SavedStatistics = SavedStatistics(),
    val message: String? = null,
) {
    val selectedProfile: TrainingProfile
        get() = profiles.firstOrNull { it.id == selectedProfileId } ?: profiles.first()
}
