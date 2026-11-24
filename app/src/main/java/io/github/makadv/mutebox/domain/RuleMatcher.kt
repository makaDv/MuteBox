package io.github.makadv.mutebox.domain

import java.text.Normalizer

enum class RuleMode {
    /** Nessuna notifica di questa app suona. */
    OFF,
    /** Tutte le notifiche di questa app suonano. */
    ALL,
    /** Suonano solo le notifiche delle persone o dei gruppi in [AppRule.senders]. */
    SOME,
}

data class AppRule(val packageName: String, val mode: RuleMode, val senders: List<String>)

object RuleMatcher {
    private val marks = Regex("\\p{M}+")
    private val nonWord = Regex("[^\\p{L}\\p{N} ]+")
    private val spaces = Regex("\\s+")

    /** Minuscole, senza accenti, senza emoji o simboli, spazi compattati. */
    fun normalize(name: String): String {
        val noAccents = Normalizer.normalize(name, Normalizer.Form.NFD).replace(marks, "")
        val cleaned = noAccents.lowercase().replace(nonWord, " ").replace(spaces, " ").trim()
        // Nome fatto solo di emoji: confronta il testo originale.
        return cleaned.ifEmpty { name.trim().lowercase() }
    }

    fun isAllowed(rule: AppRule?, sender: String?, conversation: String?): Boolean {
        if (rule == null) return false
        return when (rule.mode) {
            RuleMode.OFF -> false
            RuleMode.ALL -> true
            RuleMode.SOME -> {
                val allowed = rule.senders.map(::normalize).toSet()
                listOfNotNull(sender, conversation).any { normalize(it) in allowed }
            }
        }
    }
}
