package com.hushly.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hushly.dnd.DndController

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
        const val ACTION_STOP = "com.hushly.action.STOP"
        const val ACTION_SCHEDULE_START = "com.hushly.action.SCHEDULE_START"
        const val ACTION_SCHEDULE_END = "com.hushly.action.SCHEDULE_END"
    }
}
