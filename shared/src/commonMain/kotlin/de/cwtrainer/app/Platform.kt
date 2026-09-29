package de.cwtrainer.app

expect suspend fun playTone(frequencyHz: Double, durationMillis: Long)
expect fun loadSavedProfiles(): String?
expect fun saveProfiles(data: String)
