package io.github.makadv.mutebox.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SeenRetentionTest {
    private val day = 24 * 60 * 60 * 1000L
    private val now = 100 * day

    @Test fun recentIsKept() {
        assertFalse(SeenRetention.isExpired(now - day, now))
        assertFalse(SeenRetention.isExpired(now - 30 * day, now))
    }

    @Test fun olderThanRetentionIsExpired() {
        assertTrue(SeenRetention.isExpired(now - 31 * day, now))
    }

    @Test fun futureTimestampIsExpired() {
        // Un orologio spostato in avanti non deve rendere eterno un nome.
        assertTrue(SeenRetention.isExpired(now + day, now))
    }

    @Test fun pruneFiltersList() {
        val items = listOf("a" to now - day, "b" to now - 40 * day, "c" to now)
        assertEquals(listOf("a", "c"), SeenRetention.prune(items, now) { it.second }.map { it.first })
    }
}
