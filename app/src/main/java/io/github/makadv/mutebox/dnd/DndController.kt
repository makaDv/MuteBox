package io.github.makadv.mutebox.dnd

import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.service.quicksettings.TileService
import io.github.makadv.mutebox.mutebox

object DndController {

    fun hasDndAccess(context: Context): Boolean =
        context.getSystemService(NotificationManager::class.java).isNotificationPolicyAccessGranted

    /** Attiva MuteBox: il telefono lascia passare solo le sveglie, e quindi il suono di MuteBox. */
    fun activate(context: Context): Boolean {
        val nm = context.getSystemService(NotificationManager::class.java)
        if (!nm.isNotificationPolicyAccessGranted) return false
        val settings = context.mutebox.settings
        if (!settings.current.active) {
            val prev = nm.currentInterruptionFilter
            val restore = when (prev) {
                NotificationManager.INTERRUPTION_FILTER_UNKNOWN,
                NotificationManager.INTERRUPTION_FILTER_ALARMS -> NotificationManager.INTERRUPTION_FILTER_ALL
                else -> prev
            }
            settings.update { it.copy(active = true, previousFilter = restore) }
        }
        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
        StatusNotification.show(context)
        refreshTile(context)
        return true
    }

    fun deactivate(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        val settings = context.mutebox.settings
        if (settings.current.active && nm.isNotificationPolicyAccessGranted) {
            nm.setInterruptionFilter(settings.current.previousFilter)
        }
        settings.update { it.copy(active = false) }
        StatusNotification.cancel(context)
        refreshTile(context)
    }

    private fun refreshTile(context: Context) {
        runCatching {
            TileService.requestListeningState(context, ComponentName(context, HushTileService::class.java))
        }
    }
}
