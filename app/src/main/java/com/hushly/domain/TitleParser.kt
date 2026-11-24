package com.hushly.domain

data class ParsedTitle(val sender: String?, val conversation: String?)

/**
 * Interpreta i titoli delle notifiche di messaggistica, ad esempio quelli di WhatsApp:
 * "Nome", "Nome (3 messaggi)", "Gruppo: Nome", "Gruppo (5 messaggi): Nome".
 */
object TitleParser {
    private val countSuffix = Regex("""\s*\(\d+[^)]*\)""")

    fun stripCount(text: String): String = countSuffix.replace(text, "").trim()

    fun parse(title: String?): ParsedTitle {
        if (title.isNullOrBlank()) return ParsedTitle(null, null)
        val t = stripCount(title)
        val idx = t.indexOf(": ")
        if (idx > 0) {
            val sender = t.substring(idx + 2).trim().ifEmpty { null }
            return ParsedTitle(sender, t.substring(0, idx).trim())
        }
        return ParsedTitle(t.ifEmpty { null }, null)
    }
}
