package de.gymmane.scanner

import android.content.ClipboardManager
import android.content.Context
import androidx.compose.material3.Text
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import de.gymmane.scanner.domain.*
import de.gymmane.scanner.integration.GymManeSender
import de.gymmane.scanner.ui.ManualScreen
import de.gymmane.scanner.ui.ProductScreen
import de.gymmane.scanner.ui.theme.GymManeTheme
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Assume.assumeTrue
import org.junit.Rule
import org.junit.Test

class ImportUiTest {
    @get:Rule val compose = createComposeRule()

    @Test fun primarySendButtonUsesProductWithoutChangingPer100() {
        var sent: Product? = null
        val product = Product("test", name = "Test", servingAmount = 33.333333, servingLabel = "1 Stück",
            per100 = Nutrients(200.0, 2.5, 10.0, 5.0))
        compose.setContent { GymManeTheme { ProductScreen(product, false, false, null, { _, _ -> }, {}, {}, {}, { sent = it }) } }
        compose.onNodeWithText("Als neues Lebensmittel importieren").assertIsDisplayed().performClick()
        compose.runOnIdle { assertEquals(product, sent) }
    }

    @Test fun correctionEditorPreservesKnownOptionalValuesAndBrand() {
        var corrected: Product? = null
        val incomplete = Product("test", name = "Hafer", brand = "Testmarke",
            per100 = Nutrients(100.0, null, 10.0, 1.0, sodium = .0000123, saturated = .3, cholesterol = .002))
        compose.setContent { GymManeTheme { ManualScreen(incomplete, "", false, null, { corrected = it }, {}, forSending = true) } }
        compose.onNodeWithText("Eiweiß *").performScrollTo().performTextInput("2,5")
        compose.onNode(hasScrollToIndexAction()).performScrollToNode(hasText("Speichern und an GymMane senden"))
        compose.onNodeWithText("Speichern und an GymMane senden").performClick()
        compose.runOnIdle {
            val p = corrected!!
            assertTrue(p.name.contains("Testmarke"))
            assertEquals(.0000123, p.per100.sodium!!, 0.0)
            assertEquals(.3, p.per100.saturated!!, 0.0)
            val n = JSONObject(GymManeImport.serialize(p)).getJSONObject("food").getJSONObject("per100")
            assertEquals(.0123, n.getDouble("sodium"), .00000001)
            assertEquals(2.0, n.getDouble("cholesterol"), .000001)
        }
    }

    @Test fun changingCanVolumeUsesNeutralNameAndKeepsPer100Nutrition() {
        var sent: Product? = null
        val product = Product("can", name = "Drink", servingAmount = 500.0, servingLabel = "1 Dose",
            unit = QuantityUnit.ML, per100 = Nutrients(2.0, 0.0, .9, 0.0))
        compose.setContent { GymManeTheme { ProductScreen(product, false, false, null, { _, _ -> }, {}, {}, {}, { sent = it }) } }
        compose.onNodeWithText("Portion & Produktdetails").performScrollTo().performClick()
        compose.onNodeWithText("Portionsgröße in ml").performScrollTo().performTextReplacement("250")
        compose.onNodeWithText("Als neues Lebensmittel importieren").performClick()
        compose.runOnIdle {
            val f = JSONObject(GymManeImport.serialize(sent!!)).getJSONObject("food")
            assertEquals("1 Portion", f.getString("serving_name"))
            assertEquals(250.0, f.getDouble("grams"), 0.0)
            assertEquals(.9, f.getJSONObject("per100").getDouble("carbs"), 0.0)
        }
    }

    @Test fun portionNameCanBeEditedIndependentlyOfItsVolume() {
        var sent: Product? = null
        val product = Product("can", name = "Drink", servingAmount = 500.0, servingLabel = "500 ml",
            unit = QuantityUnit.ML, per100 = Nutrients(2.0, 0.0, .9, 0.0))
        compose.setContent { GymManeTheme { ProductScreen(product, true, false, null, { _, _ -> }, {}, {}, {}, { sent = it }) } }
        compose.onNodeWithText("Portion & Produktdetails").performScrollTo().performClick()
        compose.onNodeWithText("Bezeichnung, z. B. 1 Messlöffel").performScrollTo().performTextReplacement("1 Dose")
        compose.onNodeWithText("Als neues Lebensmittel importieren").performClick()
        compose.runOnIdle {
            val f = JSONObject(GymManeImport.serialize(sent!!)).getJSONObject("food")
            assertEquals("1 Dose", f.getString("serving_name"))
            assertEquals(500.0, f.getDouble("grams"), 0.0)
        }
    }

    @Test fun clearingAmountDoesNotSendAnOldContainerWeight() {
        var sent: Product? = null
        val product = Product("can", name = "Drink", servingAmount = 500.0, servingLabel = "1 Dose",
            unit = QuantityUnit.ML, per100 = Nutrients(2.0, 0.0, .9, 0.0))
        compose.setContent { GymManeTheme { ProductScreen(product, false, false, null, { _, _ -> }, {}, {}, {}, { sent = it }) } }
        compose.onNodeWithText("Portion & Produktdetails").performScrollTo().performClick()
        compose.onNodeWithText("Portionsgröße in ml").performScrollTo().performTextClearance()
        compose.onNodeWithText("Als neues Lebensmittel importieren").performClick()
        compose.runOnIdle {
            assertNull(sent!!.servingAmount)
            val f = JSONObject(GymManeImport.serialize(sent!!)).getJSONObject("food")
            assertEquals("1 Portion", f.getString("serving_name"))
            assertEquals(100.0, f.getDouble("grams"), 0.0)
            assertTrue(f.getBoolean("liquid"))
        }
    }

    @Test fun shareIntentContainsExactJsonWithoutChangingClipboard() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        compose.setContent { Text("Share test") }
        compose.onNodeWithText("Share test").assertIsDisplayed()
        compose.runOnIdle {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(android.content.ClipData.newPlainText("test", "unchanged"))
            val json = GymManeImport.serialize(Product("test", name = "Test", per100 = Nutrients(100.0, 1.0, 2.0, 3.0)))
            var chooser: android.content.Intent? = null
            val recording = object : android.content.ContextWrapper(context) {
                override fun startActivity(intent: android.content.Intent) { chooser = intent }
            }
            assertTrue(GymManeSender(recording).send(json).shared)
            assertEquals(android.content.Intent.ACTION_CHOOSER, chooser!!.action)
            val intent = GymManeSender(context).shareIntent(json)
            assertEquals(android.content.Intent.ACTION_SEND, intent.action)
            assertEquals("text/plain", intent.type)
            assertEquals(json, intent.getStringExtra(android.content.Intent.EXTRA_TEXT))
            assertEquals("unchanged", clipboard.primaryClip!!.getItemAt(0).text.toString())
        }
    }
}
