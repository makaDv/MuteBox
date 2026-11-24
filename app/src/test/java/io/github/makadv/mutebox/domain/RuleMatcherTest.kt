package io.github.makadv.mutebox.domain

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RuleMatcherTest {
    private fun some(vararg names: String) = AppRule("com.whatsapp", RuleMode.SOME, names.toList())

    @Test fun normalizeRemovesAccentsCaseAndEmoji() {
        assertEquals("nicolo rossi", RuleMatcher.normalize("  Nicolò   ROSSI ❤️ "))
    }

    @Test fun emojiOnlyNameIsKept() {
        assertEquals("❤️", RuleMatcher.normalize("❤️"))
    }

    @Test fun noRuleOrOffNeverAllows() {
        assertFalse(RuleMatcher.isAllowed(null, "Mamma", null))
        assertFalse(RuleMatcher.isAllowed(AppRule("x", RuleMode.OFF, listOf("Mamma")), "Mamma", null))
    }

    @Test fun allModeAllowsEverything() {
        assertTrue(RuleMatcher.isAllowed(AppRule("x", RuleMode.ALL, emptyList()), null, null))
    }

    @Test fun someModeMatchesSenderExactlyAfterNormalizing() {
        val rule = some("Mamma", "Nicolò 💙")
        assertTrue(RuleMatcher.isAllowed(rule, "mamma", null))
        assertTrue(RuleMatcher.isAllowed(rule, "Nicolo", null))
        assertFalse(RuleMatcher.isAllowed(rule, "Mamma Lucia", null))
        assertFalse(RuleMatcher.isAllowed(rule, null, null))
    }

    @Test fun someModeMatchesGroupName() {
        val rule = some("Famiglia")
        assertTrue(RuleMatcher.isAllowed(rule, "Zio Carlo", "Famiglia"))
        assertFalse(RuleMatcher.isAllowed(rule, "Zio Carlo", "Calcetto"))
    }
}
