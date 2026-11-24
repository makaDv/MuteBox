package io.github.makadv.shushlist.domain

import java.time.LocalDateTime

object ScheduleMath {
    /** true se [nowMinutes] cade nella fascia [start, end). Gestisce le fasce a cavallo della mezzanotte. */
    fun isInWindow(nowMinutes: Int, start: Int, end: Int): Boolean = when {
        start == end -> false
        start < end -> nowMinutes in start until end
        else -> nowMinutes >= start || nowMinutes < end
    }

    /** Prossimo istante (strettamente dopo [now]) in cui l'orologio segna [minutes]. */
    fun nextOccurrence(now: LocalDateTime, minutes: Int): LocalDateTime {
        val candidate = now.toLocalDate().atTime(minutes / 60, minutes % 60)
        return if (candidate.isAfter(now)) candidate else candidate.plusDays(1)
    }

    fun format(minutes: Int): String = "%02d:%02d".format(minutes / 60, minutes % 60)
}
