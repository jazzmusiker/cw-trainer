package de.cwtrainer.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.prefs.Preferences
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import kotlin.math.PI
import kotlin.math.sin

private object DesktopProfileStorage

private val profilePreferences: Preferences
    get() = Preferences.userNodeForPackage(DesktopProfileStorage::class.java)

private const val ToneSampleRate = 44_100

private data class DesktopToneSamples(
    val frequencyHz: Double,
    val speedWpm: Int,
    val dot: ByteArray,
    val dash: ByteArray,
)

private val audioLineLock = Any()
private val toneCacheLock = Any()
private val tonePlaybackMutex = Mutex()
@Volatile private var audioLine: javax.sound.sampled.SourceDataLine? = null
@Volatile private var cachedToneSamples: DesktopToneSamples? = null

actual fun loadSavedProfiles(): String? = profilePreferences.get("profiles", null)

actual fun saveProfiles(data: String) {
    profilePreferences.put("profiles", data)
}

actual suspend fun openAudioOutput() = withContext(Dispatchers.IO) {
    synchronized(audioLineLock) {
        if (audioLine?.isOpen != true) {
            val format = AudioFormat(ToneSampleRate.toFloat(), 16, 1, true, false)
            val line = AudioSystem.getSourceDataLine(format)
            line.open(format)
            line.start()
            audioLine = line
        }
    }
}

actual suspend fun configureToneSamples(frequencyHz: Double, speedWpm: Int) {
    val current = cachedToneSamples
    if (current != null && current.frequencyHz == frequencyHz && current.speedWpm == speedWpm) return

    val samples = withContext(Dispatchers.Default) {
        val dotMillis = dotDurationMillis(speedWpm)
        DesktopToneSamples(
            frequencyHz = frequencyHz,
            speedWpm = speedWpm,
            dot = createToneSamples(frequencyHz, dotMillis),
            dash = createToneSamples(frequencyHz, dotMillis * 3),
        )
    }
    synchronized(toneCacheLock) {
        cachedToneSamples = samples
    }
}

actual suspend fun playMorsePattern(pattern: String) {
    require(pattern.isNotEmpty() && pattern.all { it == '.' || it == '-' }) { "Ungültiges Morsezeichen" }
    val toneSamples = cachedToneSamples ?: error("Tonsamples wurden noch nicht vorbereitet")
    val totalBytes = pattern.sumOf { mark -> if (mark == '-') toneSamples.dash.size else toneSamples.dot.size } +
        toneSamples.dot.size * (pattern.length - 1)
    val samples = ByteArray(totalBytes)
    var offset = 0
    pattern.forEachIndexed { index, mark ->
        val tone = if (mark == '-') toneSamples.dash else toneSamples.dot
        tone.copyInto(samples, offset)
        offset += tone.size
        if (index < pattern.lastIndex) offset += toneSamples.dot.size
    }
    playSamplesAndWait(samples)
}

actual fun closeAudioOutput() {
    val line = synchronized(audioLineLock) {
        audioLine.also { audioLine = null }
    } ?: return
    runCatching { line.flush() }
    runCatching { line.stop() }
    runCatching { line.close() }
}

private fun createToneSamples(frequencyHz: Double, durationMillis: Long): ByteArray {
    val sampleCount = (ToneSampleRate * durationMillis / 1_000L).toInt().coerceAtLeast(1)
    val samples = ByteArray(sampleCount * 2)
    for (index in 0 until sampleCount) {
        val envelope = if (index < 96 || index >= sampleCount - 96) 0.72 else 1.0
        val sample = (sin(2.0 * PI * frequencyHz * index / ToneSampleRate) * Short.MAX_VALUE * envelope)
            .toInt().toShort()
        samples[index * 2] = (sample.toInt() and 0xff).toByte()
        samples[index * 2 + 1] = (sample.toInt() shr 8).toByte()
    }
    return samples
}

private suspend fun playSamplesAndWait(samples: ByteArray) {
    tonePlaybackMutex.withLock {
        withContext(Dispatchers.IO) {
            val line = audioLine ?: error("Audioausgabe ist nicht geöffnet")
            var offset = 0
            while (offset < samples.size) {
                val written = line.write(samples, offset, samples.size - offset)
                check(written > 0) { "Audioausgabe fehlgeschlagen" }
                offset += written
            }
            line.drain()
        }
    }
}
