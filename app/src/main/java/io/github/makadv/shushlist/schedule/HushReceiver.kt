package io.github.makadv.shushlist.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.makadv.shushlist.dnd.DndController

/** Azioni interne: inizio/fine fascia oraria e pulsante "Disattiva" della notifica. */
class HushReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_STOP -> DndController.deactivate(context)
            ACTION_SCHEDULE_START -> {
                DndController.activate(context)
                ScheduleManager.reschedule(context)
            }
            ACTION_SCHEDULE_END -> {
                DndController.deactivate(context)
                ScheduleManager.reschedule(context)
            }
        }
    }

    companion object {
        const val ACTION_STOP = "io.github.makadv.shushlist.action.STOP"
        const val ACTION_SCHEDULE_START = "io.github.makadv.shushlist.action.SCHEDULE_START"
        const val ACTION_SCHEDULE_END = "io.github.makadv.shushlist.action.SCHEDULE_END"
    }
}
