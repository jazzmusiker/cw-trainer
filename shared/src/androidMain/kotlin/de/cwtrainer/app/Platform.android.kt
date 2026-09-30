package de.cwtrainer.app

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.SystemClock
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.math.PI
import kotlin.math.max
import kotlin.math.sin

private object AndroidProfileStorage {
    lateinit var context: Context
}

fun initializePlatformStorage(context: Context) {
    AndroidProfileStorage.context = context.applicationContext
}

private const val ToneSampleRate = 44_100

private data class AndroidToneSamples(
    val frequencyHz: Double,
    val speedWpm: Int,
    val dot: ShortArray,
    val dash: ShortArray,
)

private open class AudioPlaybackException(message: String, cause: Throwable? = null) : IllegalStateException(message, cause)
private class AudioPlaybackTimeoutException(message: String) : AudioPlaybackException(message)

private val audioTrackLock = Any()
private val toneCacheLock = Any()
private val tonePlaybackMutex = Mutex()
@Volatile private var audioTrack: AudioTrack? = null
@Volatile private var cachedToneSamples: AndroidToneSamples? = null

actual fun loadSavedProfiles(): String? =
    AndroidProfileStorage.context.getSharedPreferences("cw-trainer", Context.MODE_PRIVATE)
        .getString("profiles", null)

actual fun saveProfiles(data: String) {
    AndroidProfileStorage.context.getSharedPreferences("cw-trainer", Context.MODE_PRIVATE)
        .edit().putString("profiles", data).apply()
}

actual fun loadSavedStatistics(): String? =
    AndroidProfileStorage.context.getSharedPreferences("cw-trainer", Context.MODE_PRIVATE)
        .getString("statistics", null)

actual fun saveStatistics(data: String) {
    val saved = AndroidProfileStorage.context.getSharedPreferences("cw-trainer", Context.MODE_PRIVATE)
        .edit().putString("statistics", data).commit()
    check(saved) { "Statistik konnte nicht gespeichert werden" }
}

actual suspend fun openAudioOutput() = withContext(Dispatchers.IO) {
    synchronized(audioTrackLock) {
        if (audioTrack?.state != AudioTrack.STATE_INITIALIZED) {
            audioTrack?.let { staleTrack -> runCatching { staleTrack.release() } }
            audioTrack = null

            val format = AudioFormat.Builder()
                .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                .setSampleRate(ToneSampleRate)
                .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                .build()
            val minimumBufferBytes = AudioTrack.getMinBufferSize(
                ToneSampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT,
            )
            check(minimumBufferBytes > 0) { "Audioausgabe konnte nicht initialisiert werden" }

            val track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(format)
                .setBufferSizeInBytes(max(minimumBufferBytes, ToneSampleRate * 2 / 5))
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()
            if (track.state != AudioTrack.STATE_INITIALIZED) {
                track.release()
                error("Audioausgabe konnte nicht initialisiert werden")
            }
            audioTrack = track
        }
    }
}

actual suspend fun configureToneSamples(frequencyHz: Double, speedWpm: Int) {
    val current = cachedToneSamples
    if (current != null && current.frequencyHz == frequencyHz && current.speedWpm == speedWpm) return

    val samples = withContext(Dispatchers.Default) {
        val dotMillis = dotDurationMillis(speedWpm)
        AndroidToneSamples(
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
    val totalSamples = pattern.sumOf { mark -> if (mark == '-') toneSamples.dash.size else toneSamples.dot.size } +
        toneSamples.dot.size * (pattern.length - 1)
    val samples = ShortArray(totalSamples)
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
    val track = synchronized(audioTrackLock) {
        audioTrack.also { audioTrack = null }
    } ?: return
    runCatching { track.pause() }
    runCatching { track.flush() }
    runCatching { track.stop() }
    runCatching { track.release() }
}

private fun createToneSamples(frequencyHz: Double, durationMillis: Long): ShortArray {
    val sampleCount = (ToneSampleRate * durationMillis / 1_000L).toInt().coerceAtLeast(1)
    return ShortArray(sampleCount) { index ->
        val envelope = if (index < 96 || index >= sampleCount - 96) 0.72 else 1.0
        (sin(2.0 * PI * frequencyHz * index / ToneSampleRate) * Short.MAX_VALUE * envelope)
            .toInt().toShort()
    }
}

private suspend fun playSamplesAndWait(samples: ShortArray) {
    tonePlaybackMutex.withLock {
        try {
            playSamplesOnCurrentTrack(samples)
        } catch (failure: AudioPlaybackException) {
            closeAudioOutput()
            openAudioOutput()
            playSamplesOnCurrentTrack(samples)
        }
    }
}

private suspend fun playSamplesOnCurrentTrack(samples: ShortArray) {
    withContext(Dispatchers.IO) {
        val coroutineContext = currentCoroutineContext()
        val track = audioTrack ?: error("Audioausgabe ist nicht geöffnet")
        val startFrame = track.playbackHeadPosition.toLong() and 0xFFFF_FFFFL
        val expectedDurationMillis = samples.size * 1_000L / ToneSampleRate
        val deadline = SystemClock.elapsedRealtime() + expectedDurationMillis + 2_000L
        var offset = 0
        var playbackStarted = false
        while (offset < samples.size) {
            coroutineContext.ensureActive()
            if (SystemClock.elapsedRealtime() >= deadline) {
                throw AudioPlaybackTimeoutException("Zeitüberschreitung beim Schreiben der Audiodaten")
            }
            if (track.state != AudioTrack.STATE_INITIALIZED) {
                throw AudioPlaybackException("Audioausgabe wurde beendet")
            }
            val written = try {
                track.write(samples, offset, samples.size - offset, AudioTrack.WRITE_NON_BLOCKING)
            } catch (failure: Exception) {
                throw AudioPlaybackException("Audiodaten konnten nicht geschrieben werden", failure)
            }
            if (written < 0) throw AudioPlaybackException("Audioausgabe fehlgeschlagen ($written)")
            if (written == 0) {
                delay(2)
            } else {
                offset += written
                if (!playbackStarted) {
                    try {
                        track.play()
                    } catch (failure: Exception) {
                        throw AudioPlaybackException("Audiowiedergabe konnte nicht gestartet werden", failure)
                    }
                    playbackStarted = true
                }
            }
        }

        while (true) {
            coroutineContext.ensureActive()
            if (SystemClock.elapsedRealtime() >= deadline) {
                throw AudioPlaybackTimeoutException("Zeitüberschreitung bei der Audiowiedergabe")
            }
            if (track.state != AudioTrack.STATE_INITIALIZED) {
                throw AudioPlaybackException("Audioausgabe wurde beendet")
            }
            val playedFrames = ((track.playbackHeadPosition.toLong() and 0xFFFF_FFFFL) - startFrame) and 0xFFFF_FFFFL
            if (playedFrames >= samples.size) break
            delay(2)
        }
    }
}
