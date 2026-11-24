package io.github.makadv.shushlist.service

import android.app.Notification
import android.app.NotificationManager
import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import io.github.makadv.shushlist.dnd.DndController
import io.github.makadv.shushlist.domain.RuleMatcher
import io.github.makadv.shushlist.shushlist

class HushListenerService : NotificationListenerService() {

    /** Ultimo contenuto visto per ogni notifica: WhatsApp ripubblica la stessa notifica senza nuovi messaggi. */
    private val lastSignature = HashMap<String, String>()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return
        val n = sbn.notification
        if (sbn.isOngoing) return
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

        val app = shushlist
        val info = SenderExtractor.extract(n)
        app.rules.recordSeen(sbn.packageName, info.names)

        if (!app.settings.current.active) return
        if (isSilentChannel(sbn)) return

        val rule = app.rules.ruleFor(sbn.packageName)
        if (!RuleMatcher.isAllowed(rule, info.sender, info.conversation)) return

        val text = n.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val signature = "${n.`when`}|$text"
        if (lastSignature[sbn.key] == signature) return
        if (lastSignature.size > 500) lastSignature.clear()
        lastSignature[sbn.key] = signature

        AlertPlayer.play(this, "${sbn.packageName}|${info.conversation ?: info.sender ?: sbn.key}")
    }

    /** Le notifiche su canali silenziosi (importanza bassa) non devono mai suonare. */
    private fun isSilentChannel(sbn: StatusBarNotification): Boolean {
        val ranking = Ranking()
        val found = runCatching { currentRanking?.getRanking(sbn.key, ranking) == true }.getOrDefault(false)
        if (!found) return false
        val importance = ranking.importance
        return importance != NotificationManager.IMPORTANCE_UNSPECIFIED &&
            importance < NotificationManager.IMPORTANCE_DEFAULT
    }

    override fun onListenerDisconnected() {
        super.onListenerDisconnected()
        // Senza ascolto Shushlist non può far suonare nulla: meglio togliere il silenzio che perdere notifiche importanti.
        if (shushlist.settings.current.active) DndController.deactivate(this)
        requestRebind(ComponentName(this, HushListenerService::class.java))
    }
}
