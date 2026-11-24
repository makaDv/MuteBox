package com.hushly.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDateTime

class ScheduleMathTest {
    @Test fun windowAcrossMidnight() {
        val start = 23 * 60
        val end = 7 * 60
        assertTrue(ScheduleMath.isInWindow(23 * 60 + 30, start, end))
        assertTrue(ScheduleMath.isInWindow(3 * 60, start, end))
        assertFalse(ScheduleMath.isInWindow(7 * 60, start, end))
        assertFalse(ScheduleMath.isInWindow(12 * 60, start, end))
    }

    @Test fun windowSameDay() {
        assertTrue(ScheduleMath.isInWindow(14 * 60, 13 * 60, 15 * 60))
        assertFalse(ScheduleMath.isInWindow(15 * 60, 13 * 60, 15 * 60))
    }

    @Test fun emptyWindow() = assertFalse(ScheduleMath.isInWindow(100, 100, 100))

    @Test fun nextOccurrenceTodayOrTomorrow() {
        val now = LocalDateTime.of(2026, 11, 24, 22, 0)
        assertEquals(LocalDateTime.of(2026, 11, 24, 23, 0), ScheduleMath.nextOccurrence(now, 23 * 60))
        assertEquals(LocalDateTime.of(2026, 11, 25, 7, 0), ScheduleMath.nextOccurrence(now, 7 * 60))
        assertEquals(LocalDateTime.of(2026, 11, 25, 22, 0), ScheduleMath.nextOccurrence(now, 22 * 60))
    }
}
