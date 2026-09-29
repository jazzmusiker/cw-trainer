package de.cwtrainer.app

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.PI
import kotlin.math.sin

private object AndroidProfileStorage {
    lateinit var context: Context
}

fun initializePlatformStorage(context: Context) {
    AndroidProfileStorage.context = context.applicationContext
}

actual fun loadSavedProfiles(): String? =
    AndroidProfileStorage.context.getSharedPreferences("cw-trainer", Context.MODE_PRIVATE)
        .getString("profiles", null)

actual fun saveProfiles(data: String) {
    AndroidProfileStorage.context.getSharedPreferences("cw-trainer", Context.MODE_PRIVATE)
        .edit().putString("profiles", data).apply()
}

actual suspend fun playTone(frequencyHz: Double, durationMillis: Long) = withContext(Dispatchers.IO) {
    val sampleRate = 44_100
    val sampleCount = (sampleRate * durationMillis / 1_000L).toInt().coerceAtLeast(1)
    val samples = ShortArray(sampleCount)
    for (i in samples.indices) {
        val envelope = if (i < 96 || i >= sampleCount - 96) 0.72 else 1.0
        samples[i] = (sin(2.0 * PI * frequencyHz * i / sampleRate) * Short.MAX_VALUE * envelope).toInt().toShort()
    }

    val format = AudioFormat.Builder()
        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
        .setSampleRate(sampleRate)
        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
        .build()
    val track = AudioTrack.Builder()
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()
        )
        .setAudioFormat(format)
        .setBufferSizeInBytes(samples.size * 2)
        .setTransferMode(AudioTrack.MODE_STATIC)
        .build()
    try {
        track.write(samples, 0, samples.size, AudioTrack.WRITE_BLOCKING)
        track.play()
        Thread.sleep(durationMillis)
    } finally {
        track.stop()
        track.release()
    }
}
