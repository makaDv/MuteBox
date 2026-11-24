package io.github.makadv.mutebox.data

import android.app.NotificationManager
import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class HushSettings(
    val active: Boolean = false,
    /** Filtro "Non disturbare" attivo prima di MuteBox, da ripristinare allo spegnimento. */
    val previousFilter: Int = NotificationManager.INTERRUPTION_FILTER_ALL,
    val scheduleEnabled: Boolean = false,
    val startMinutes: Int = 23 * 60,
    val endMinutes: Int = 7 * 60,
    /** null = suono di notifica predefinito del telefono. */
    val soundUri: String? = null,
    val volume: Int = 80,
    val vibrate: Boolean = true,
)

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE)
    private val _state = MutableStateFlow(load())
    val state: StateFlow<HushSettings> = _state.asStateFlow()
    val current: HushSettings get() = _state.value

    @Synchronized
    fun update(transform: (HushSettings) -> HushSettings) {
        val next = transform(_state.value)
        save(next)
        _state.value = next
    }

    private fun load(): HushSettings {
        val d = HushSettings()
        return HushSettings(
            active = prefs.getBoolean("active", d.active),
            previousFilter = prefs.getInt("previousFilter", d.previousFilter),
            scheduleEnabled = prefs.getBoolean("scheduleEnabled", d.scheduleEnabled),
            startMinutes = prefs.getInt("startMinutes", d.startMinutes),
            endMinutes = prefs.getInt("endMinutes", d.endMinutes),
            soundUri = prefs.getString("soundUri", d.soundUri),
            volume = prefs.getInt("volume", d.volume),
            vibrate = prefs.getBoolean("vibrate", d.vibrate),
        )
    }

    private fun save(s: HushSettings) {
        // commit() e non apply(): lo stato "attivo" deve sopravvivere a un kill immediato del processo.
        prefs.edit()
            .putBoolean("active", s.active)
            .putInt("previousFilter", s.previousFilter)
            .putBoolean("scheduleEnabled", s.scheduleEnabled)
            .putInt("startMinutes", s.startMinutes)
            .putInt("endMinutes", s.endMinutes)
            .putString("soundUri", s.soundUri)
            .putInt("volume", s.volume)
            .putBoolean("vibrate", s.vibrate)
            .commit()
    }
}
