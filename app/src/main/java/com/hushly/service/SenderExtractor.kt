package com.hushly.service

import android.app.Notification
import androidx.core.app.NotificationCompat
import com.hushly.domain.TitleParser

data class SenderInfo(val sender: String?, val conversation: String?) {
    val names: List<String> get() = listOfNotNull(sender, conversation).distinct()
}

object SenderExtractor {
    fun extract(n: Notification): SenderInfo {
        val title = (n.extras.getCharSequence(Notification.EXTRA_TITLE)
            ?: n.extras.getCharSequence(Notification.EXTRA_CONVERSATION_TITLE))?.toString()
        val parsed = TitleParser.parse(title)

        val style = runCatching { NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(n) }.getOrNull()
        if (style != null) {
            // Il mittente del messaggio più recente; il titolo della conversazione è il nome del gruppo.
            val person = style.messages.lastOrNull()?.person?.name?.toString()?.trim()?.ifEmpty { null }
            val conversation = style.conversationTitle?.toString()?.let(TitleParser::stripCount)?.ifEmpty { null }
            if (person != null || conversation != null) {
                return SenderInfo(person ?: parsed.sender, conversation ?: parsed.conversation)
            }
        }
        return SenderInfo(parsed.sender, parsed.conversation)
    }
}
