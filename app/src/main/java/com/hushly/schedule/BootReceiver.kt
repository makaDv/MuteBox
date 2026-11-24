package com.hushly.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.hushly.dnd.StatusNotification
import com.hushly.hushly

/** Dopo riavvio, aggiornamento dell'app o cambio d'ora: riprogramma gli allarmi. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ScheduleManager.reschedule(context)
        if (context.hushly.settings.current.active) {
            StatusNotification.show(context)
        } else {
            ScheduleManager.applyCurrentWindow(context)
        }
    }
}
