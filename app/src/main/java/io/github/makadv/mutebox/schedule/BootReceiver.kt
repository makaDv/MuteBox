package io.github.makadv.mutebox.schedule

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.makadv.mutebox.dnd.StatusNotification
import io.github.makadv.mutebox.mutebox

/** Dopo riavvio, aggiornamento dell'app o cambio d'ora: riprogramma gli allarmi. */
class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        ScheduleManager.reschedule(context)
        if (context.mutebox.settings.current.active) {
            StatusNotification.show(context)
        } else {
            ScheduleManager.applyCurrentWindow(context)
        }
    }
}
