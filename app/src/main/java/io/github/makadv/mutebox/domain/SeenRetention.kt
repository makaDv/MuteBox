package io.github.makadv.mutebox.domain

/**
 * Per quanto tempo restano salvati i nomi visti nelle notifiche (limitazione della conservazione, art. 5.1.e GDPR).
 */
object SeenRetention {
    const val RETENTION_MS = 30L * 24 * 60 * 60 * 1000

    fun isExpired(lastSeen: Long, now: Long, retention: Long = RETENTION_MS): Boolean =
        lastSeen > now || now - lastSeen > retention

    /** Tiene solo gli elementi ancora validi. */
    fun <T> prune(items: List<T>, now: Long, retention: Long = RETENTION_MS, lastSeen: (T) -> Long): List<T> =
        items.filterNot { isExpired(lastSeen(it), now, retention) }
}
