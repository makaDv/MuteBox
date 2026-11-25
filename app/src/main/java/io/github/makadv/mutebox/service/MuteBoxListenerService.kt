package io.github.makadv.mutebox.service

import android.app.Notification
import android.app.NotificationManager
import android.content.ComponentName
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import io.github.makadv.mutebox.dnd.DndController
import io.github.makadv.mutebox.domain.RuleMatcher
import io.github.makadv.mutebox.domain.RuleMode
import io.github.makadv.mutebox.mutebox

class MuteBoxListenerService : NotificationListenerService() {

    /** Ultimo contenuto visto per ogni notifica: WhatsApp ripubblica la stessa notifica senza nuovi messaggi. */
    private val lastSignature = HashMap<String, String>()

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName) return
        val n = sbn.notification
        if (sbn.isOngoing) return
        if (n.flags and Notification.FLAG_GROUP_SUMMARY != 0) return

        val app = mutebox
        val rule = app.rules.ruleFor(sbn.packageName)
        // Minimizzazione: nessun dato letto o salvato per le app senza regole (banca, salute, ecc.).
        if (rule == null || rule.mode == RuleMode.OFF) return

        val info = SenderExtractor.extract(n)
        // I nomi si ricordano solo dove servono a scegliere le persone.
        if (rule.mode == RuleMode.SOME) app.rules.recordSeen(sbn.packageName, info.names)

        if (!app.settings.current.active) return
        if (isSilentChannel(sbn)) return

        if (!RuleMatcher.isAllowed(rule, info.sender, info.conversation)) return

        // Del testo resta in memoria solo un hash, per riconoscere le ripubblicazioni identiche.
        val textHash = n.extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty().hashCode()
        val signature = "${n.`when`}|$textHash"
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
        // Senza ascolto MuteBox non può far suonare nulla: meglio togliere il silenzio che perdere notifiche importanti.
        if (mutebox.settings.current.active) DndController.deactivate(this)
        requestRebind(ComponentName(this, MuteBoxListenerService::class.java))
    }
}
