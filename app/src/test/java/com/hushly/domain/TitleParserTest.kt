package com.hushly.domain

import org.junit.Assert.assertEquals
import org.junit.Test

class TitleParserTest {
    @Test fun plainName() = assertEquals(ParsedTitle("Mamma", null), TitleParser.parse("Mamma"))

    @Test fun nameWithCount() = assertEquals(ParsedTitle("Mamma", null), TitleParser.parse("Mamma (3 messaggi)"))

    @Test fun groupAndSender() = assertEquals(ParsedTitle("Zio Carlo", "Famiglia"), TitleParser.parse("Famiglia: Zio Carlo"))

    @Test fun groupWithCountAndSender() =
        assertEquals(ParsedTitle("Zio Carlo", "Famiglia"), TitleParser.parse("Famiglia (5 messaggi): Zio Carlo"))

    @Test fun nonNumericParenthesisIsKept() =
        assertEquals(ParsedTitle("Marco (lavoro)", null), TitleParser.parse("Marco (lavoro)"))

    @Test fun blank() = assertEquals(ParsedTitle(null, null), TitleParser.parse("  "))
}
