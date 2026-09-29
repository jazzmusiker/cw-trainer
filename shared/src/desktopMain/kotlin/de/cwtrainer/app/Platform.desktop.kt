package de.cwtrainer.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.prefs.Preferences
import javax.sound.sampled.AudioFormat
import javax.sound.sampled.AudioSystem
import kotlin.math.PI
import kotlin.math.sin

private object DesktopProfileStorage

private val profilePreferences: Preferences
    get() = Preferences.userNodeForPackage(DesktopProfileStorage::class.java)

actual fun loadSavedProfiles(): String? = profilePreferences.get("profiles", null)

actual fun saveProfiles(data: String) {
    profilePreferences.put("profiles", data)
}

actual suspend fun playTone(frequencyHz: Double, durationMillis: Long) = withContext(Dispatchers.IO) {
    val sampleRate = 44_100
    val sampleCount = (sampleRate * durationMillis / 1_000L).toInt().coerceAtLeast(1)
    val audioBytes = ByteArray(sampleCount * 2)
    for (i in 0 until sampleCount) {
        val envelope = if (i < 96 || i >= sampleCount - 96) 0.72 else 1.0
        val sample = (sin(2.0 * PI * frequencyHz * i / sampleRate) * Short.MAX_VALUE * envelope).toInt().toShort()
        audioBytes[i * 2] = (sample.toInt() and 0xff).toByte()
        audioBytes[i * 2 + 1] = (sample.toInt() shr 8).toByte()
    }
    val format = AudioFormat(sampleRate.toFloat(), 16, 1, true, false)
    val line = AudioSystem.getSourceDataLine(format)
    line.open(format)
    line.start()
    try {
        var offset = 0
        while (offset < audioBytes.size) {
            offset += line.write(audioBytes, offset, audioBytes.size - offset)
        }
        line.drain()
    } finally {
        line.stop()
        line.close()
    }
}
