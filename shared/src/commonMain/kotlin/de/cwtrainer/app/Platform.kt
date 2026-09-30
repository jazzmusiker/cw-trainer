package de.cwtrainer.app

expect suspend fun openAudioOutput()
expect suspend fun configureToneSamples(frequencyHz: Double, speedWpm: Int)
expect suspend fun playMorsePattern(pattern: String)
expect fun closeAudioOutput()
expect fun loadSavedProfiles(): String?
expect fun saveProfiles(data: String)
expect fun loadSavedStatistics(): String?
expect fun saveStatistics(data: String)

private val appLifecycleObservers = mutableSetOf<(Boolean) -> Unit>()

fun registerAppLifecycleObserver(observer: (Boolean) -> Unit): () -> Unit {
    appLifecycleObservers += observer
    return { appLifecycleObservers -= observer }
}

fun notifyAppVisibilityChanged(isVisible: Boolean) {
    appLifecycleObservers.toList().forEach { it(isVisible) }
}

internal fun dotDurationMillis(speedWpm: Int): Long =
    (1_200.0 / speedWpm).toLong().coerceAtLeast(1L)
