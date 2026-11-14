package me.weishu.kernelsu.ui.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemeTextConfigTest {

    @Test
    fun versionPlaceholderRoundTrip() {
        val key = CustomTextKey.HOME_UPDATE_NEW_VERSION
        val template = "Update to %s now"
        val parts = key.splitTemplate(template)
        assertEquals(listOf("Update to ", " now"), parts)
        assertEquals("Update to %s now", key.joinTemplate(parts))
    }

    @Test
    fun missingPlaceholderAppendsArgument() {
        assertEquals("Custom text 123", formatCustomText("Custom text", listOf(123)))
    }

    @Test
    fun malformedTemplateKeepsOriginalText() {
        val template = "Broken %d placeholder"
        assertEquals(template, formatCustomText(template, listOf("text")))
    }

    @Test
    fun keysAreUniqueAndConsumed() {
        val keys = CustomTextKey.entries.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
        assertTrue(keys.all { it.contains('.') })
    }
}
