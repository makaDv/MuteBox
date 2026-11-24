package io.github.makadv.shushlist.schedule

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import io.github.makadv.shushlist.dnd.DndController
import io.github.makadv.shushlist.domain.ScheduleMath
import io.github.makadv.shushlist.shushlist
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

object ScheduleManager {
    private const val REQ_START = 10
    private const val REQ_END = 11

    /** Cancella e riprogramma gli allarmi di inizio e fine fascia. */
    fun reschedule(context: Context) {
        val am = context.getSystemService(AlarmManager::class.java)
        am.cancel(pending(context, REQ_START, HushReceiver.ACTION_SCHEDULE_START))
        am.cancel(pending(context, REQ_END, HushReceiver.ACTION_SCHEDULE_END))
        val s = context.shushlist.settings.current
        if (!s.scheduleEnabled || s.startMinutes == s.endMinutes) return
        val now = LocalDateTime.now()
        set(context, am, REQ_START, HushReceiver.ACTION_SCHEDULE_START, ScheduleMath.nextOccurrence(now, s.startMinutes))
        set(context, am, REQ_END, HushReceiver.ACTION_SCHEDULE_END, ScheduleMath.nextOccurrence(now, s.endMinutes))
    }

    fun isNowInWindow(context: Context): Boolean {
        val s = context.shushlist.settings.current
        if (!s.scheduleEnabled) return false
        val now = LocalTime.now()
        return ScheduleMath.isInWindow(now.hour * 60 + now.minute, s.startMinutes, s.endMinutes)
    }

    /** Se ora siamo dentro la fascia oraria, accende Shushlist. */
    fun applyCurrentWindow(context: Context) {
        if (isNowInWindow(context) && !context.shushlist.settings.current.active) {
            DndController.activate(context)
        }
    }

    fun canScheduleExact(context: Context): Boolean =
        Build.VERSION.SDK_INT < 31 || context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()

    private fun set(context: Context, am: AlarmManager, req: Int, action: String, at: LocalDateTime) {
        val millis = at.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pi = pending(context, req, action)
        if (canScheduleExact(context)) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
        } else {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, millis, pi)
        }
    }

    private fun pending(context: Context, req: Int, action: String): PendingIntent =
        PendingIntent.getBroadcast(
            context, req,
            Intent(context, HushReceiver::class.java).setAction(action),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
}
