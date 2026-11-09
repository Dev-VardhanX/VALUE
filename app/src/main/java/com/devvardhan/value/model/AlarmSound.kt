package com.devvardhan.value.model

/**
 * Models alarm sounds available in VALUE:
 * 1. Default (System alarm ringtone)
 * 2. Value Tones (Procedural built-in minimalist tones)
 * 3. My Sounds (Imported local audio files via Storage Access Framework)
 */
sealed class AlarmSound(
    val id: String,
    val displayName: String
) {
    /**
     * System default alarm sound via RingtoneManager.TYPE_ALARM
     */
    data object PhoneDefault : AlarmSound(ID_PHONE_DEFAULT, "Phone default")

    /**
     * Built-in synthesized minimalist tones
     */
    data class BuiltInTone(
        val toneKey: String,
        val title: String
    ) : AlarmSound("builtin_$toneKey", title)

    /**
     * User-imported audio file from local phone storage
     */
    data class UserSound(
        val uriString: String,
        val name: String,
        val sizeBytes: Long = 0L,
        val importTimestamp: Long = 0L
    ) : AlarmSound(uriString, name)

    companion object {
        const val ID_PHONE_DEFAULT = "phone_default"

        val BUILT_IN_TONES = listOf(
            BuiltInTone("chime", "Chime"),
            BuiltInTone("digital", "Digital"),
            BuiltInTone("radar", "Radar"),
            BuiltInTone("pulse", "Pulse"),
            BuiltInTone("bell", "Bell")
        )

        fun findById(id: String, importedSounds: List<UserSound>): AlarmSound {
            if (id == ID_PHONE_DEFAULT) return PhoneDefault
            val builtin = BUILT_IN_TONES.find { it.id == id }
            if (builtin != null) return builtin
            val user = importedSounds.find { it.id == id }
            if (user != null) return user
            return PhoneDefault
        }
    }
}

