package de.gymmane.scanner

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.test.platform.app.InstrumentationRegistry
import de.gymmane.scanner.ui.theme.GymManeTheme
import de.gymmane.scanner.updates.*
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test

class ReleaseNotesUiTest {
    @get:Rule val compose = createComposeRule()

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private val markdown = """
        ## Release highlights

        **Better scanning** and *clearer results* with `ZXing`.

        - First improvement
        - Second improvement

        [Release link](https://github.com/KingMezzo/GymMane-Scanner/releases)
    """.trimIndent()

    private fun awaitText(text: String) {
        compose.waitUntil(10_000) {
            compose.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    @Test fun whatsNewRendersFormattingListsAndClickableLinks() {
        var opened = ""
        var continued = false
        compose.setContent {
            GymManeTheme {
                CompositionLocalProvider(LocalUriHandler provides object : UriHandler {
                    override fun openUri(uri: String) { opened = uri }
                }) {
                    WhatsNewScreen(UpdateState(notes = markdown), { continued = true })
                }
            }
        }
        awaitText("Release highlights")
        compose.onNodeWithText("Release highlights").assertIsDisplayed()
        compose.onNodeWithText("**Better scanning**", substring = true).assertDoesNotExist()
        val paragraph = compose.onNodeWithText("Better scanning", substring = true)
            .fetchSemanticsNode().config[SemanticsProperties.Text].single()
        assertTrue(paragraph.spanStyles.any { it.item.fontWeight == FontWeight.Bold })
        assertTrue(paragraph.spanStyles.any { it.item.fontFamily == FontFamily.Monospace })
        compose.onNodeWithText("First improvement").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Second improvement").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText("Release link").performScrollTo().performClick()
        compose.runOnIdle { assertEquals("https://github.com/KingMezzo/GymMane-Scanner/releases", opened) }
        compose.onNodeWithText(context.getString(R.string.update_done)).performClick()
        compose.runOnIdle { assertTrue(continued) }
    }

    @Test fun updatePopupRendersMarkdownAndScrollsWhileActionsStayVisible() {
        var updated = false
        val notes = markdown + "\n\n" + (1..60).joinToString("\n\n") { "Change $it" } + "\n\n**Last change**"
        val release = Release("v9.0.0", SemVer.parse("9.0.0")!!, notes, "https://example.com/app.apk", 4, null)
        compose.setContent {
            GymManeTheme { UpdateOffer(UpdateState(offer = release), true, {}, { updated = true }) }
        }
        awaitText("Release highlights")
        compose.onNodeWithText("Release highlights").assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.update_action)).assertIsDisplayed()
        compose.onNodeWithText("Last change").performScrollTo().assertIsDisplayed()
        compose.onNodeWithText(context.getString(R.string.update_action)).assertIsDisplayed().performClick()
        compose.runOnIdle { assertTrue(updated) }
    }

    @Test fun whatsNewKeepsOfflineNotesAndLineBreaks() {
        compose.setContent { GymManeTheme { WhatsNewScreen(UpdateState(notes = "  "), {}) } }
        compose.onNodeWithText(context.getString(R.string.update_local_notes)).assertIsDisplayed()
    }

    @Test fun updatePopupHandlesEmptyReleaseNotes() {
        val release = Release("v9.0.0", SemVer.parse("9.0.0")!!, "", "https://example.com/app.apk", 4, null)
        compose.setContent { GymManeTheme { UpdateOffer(UpdateState(offer = release), true, {}, {}) } }
        val fallback = context.getString(R.string.update_no_notes)
        awaitText(fallback)
        compose.onNodeWithText(fallback).assertIsDisplayed()
    }
}
