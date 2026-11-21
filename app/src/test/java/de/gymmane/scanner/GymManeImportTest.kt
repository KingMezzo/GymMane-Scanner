package de.gymmane.scanner

import de.gymmane.scanner.domain.*
import de.gymmane.scanner.data.ProductJson
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class GymManeImportTest {
    private val product = Product("test", name = "Haferdrink", brand = "Testmarke",
        per100 = Nutrients(kcal = 42.5, protein = 1.2, carbs = 6.5, fat = 1.0))
    private fun food(p: Product = product) = JSONObject(GymManeImport.serialize(p)).getJSONObject("food")

    @Test fun `strict envelope numbers and required keys use JSON serializer independent of locale`() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.GERMANY)
            val json = GymManeImport.serialize(product)
            val root = JSONObject(json)
            assertEquals(setOf("gymmane", "v", "food"), root.keys().asSequence().toSet())
            assertEquals("gymmane-food", root.getString("gymmane"))
            assertEquals(1, root.getInt("v"))
            val f = root.getJSONObject("food")
            assertEquals("Haferdrink · Testmarke", f.getString("name"))
            assertEquals("1 Portion", f.getString("serving_name"))
            assertEquals(100.0, f.getDouble("grams"), 0.0)
            assertFalse(f.getBoolean("liquid"))
            assertTrue(f.getJSONObject("per100").get("kcal") is Number)
            assertTrue(json.contains("42.5"))
            assertFalse(json.contains("42,5"))
            assertEquals(setOf("kcal", "protein", "carbs", "fat"), f.getJSONObject("per100").keys().asSequence().toSet())
        } finally { Locale.setDefault(previous) }
    }

    @Test fun `known portion affects grams only never per100 values`() {
        val f = food(product.copy(servingAmount = 250.0, servingLabel = "1 Glas", unit = QuantityUnit.ML))
        assertEquals(250.0, f.getDouble("grams"), 0.0)
        assertEquals("1 Glas", f.getString("serving_name"))
        assertTrue(f.getBoolean("liquid"))
        assertEquals(42.5, f.getJSONObject("per100").getDouble("kcal"), 0.0)
    }

    @Test fun `serving label without a known mass falls back exactly as specified`() {
        val f = food(product.copy(servingLabel = "1 Stück", servingAmount = null, unit = QuantityUnit.ML))
        assertEquals("1 Portion", f.getString("serving_name"))
        assertEquals(100.0, f.getDouble("grams"), 0.0)
        assertTrue(f.getBoolean("liquid"))
    }

    @Test fun `every known optional nutrient is included and mg conversion occurs once`() {
        val n = product.per100.copy(fiber = 0.0, sugar = 1.2, saturated = .4, trans = .01,
            monounsaturated = .2, polyunsaturated = .3, sodium = .0428, cholesterol = .003)
        val p = ProductJson.decode(ProductJson.encode(product.copy(per100 = n)))
        val out = food(p).getJSONObject("per100")
        assertEquals(42.8, out.getDouble("sodium"), .000001)
        assertEquals(3.0, out.getDouble("cholesterol"), .000001)
        assertEquals(0.0, out.getDouble("fiber"), 0.0)
        assertEquals(.01, out.getDouble("trans"), 0.0)
        assertEquals(.4, out.getDouble("saturated"), 0.0)
        assertEquals(.2, out.getDouble("monounsaturated"), 0.0)
        assertEquals(.3, out.getDouble("polyunsaturated"), 0.0)
        assertFalse(out.has("salt"))
    }

    @Test fun `salt only yields sodium in milligrams while explicit sodium zero wins`() {
        assertEquals(400.0, food(product.copy(per100 = product.per100.copy(salt = 1.0)))
            .getJSONObject("per100").getDouble("sodium"), 0.0)
        assertEquals(0.0, food(product.copy(per100 = product.per100.copy(salt = 1.0, sodium = 0.0)))
            .getJSONObject("per100").getDouble("sodium"), 0.0)
    }

    @Test fun `unknown invalid optional values are omitted not encoded as null or zero`() {
        val out = food(product.copy(per100 = product.per100.copy(fiber = Double.NaN, sugar = -1.0,
            cholesterol = Double.POSITIVE_INFINITY))).getJSONObject("per100")
        assertFalse(out.has("fiber")); assertFalse(out.has("sugar")); assertFalse(out.has("cholesterol"))
    }

    @Test fun `each required value must be present finite and nonnegative while genuine zero works`() {
        val bad = listOf(product.copy(name = ""), product.copy(nameKnown = false),
            product.copy(servingAmount = 0.0), product.copy(servingAmount = Double.NaN),
            product.copy(per100 = product.per100.copy(kcal = null)),
            product.copy(per100 = product.per100.copy(protein = null)),
            product.copy(per100 = product.per100.copy(carbs = Double.NaN)),
            product.copy(per100 = product.per100.copy(fat = -1.0)))
        bad.forEach {
            try { GymManeImport.serialize(it); fail("Incomplete product should be rejected") }
            catch (e: IncompleteGymManeFood) { assertTrue(e.fields.isNotEmpty()) }
        }
        assertEquals(0.0, food(product.copy(per100 = Nutrients(0.0, 0.0, 0.0, 0.0)))
            .getJSONObject("per100").getDouble("kcal"), 0.0)
    }

    @Test fun `names with quotes and newlines are escaped and brand is not duplicated`() {
        val p = product.copy(name = "Testmarke \"Hafer\"\nDrink", brand = "Testmarke")
        assertEquals(p.name, food(p).getString("name"))
    }

    @Test fun `OFF optional nutrients map as normalized grams and survive the offline cache`() {
        val p = ProductJson.fromApi("""{"status":1,"product":{"product_name_de":"Test","nutriments":{"energy-kcal_100g":42,"proteins_100g":1,"carbohydrates_100g":4,"fat_100g":1,"salt_100g":0.1,"saturated-fat_100g":0.2,"trans-fat_100g":0.01,"monounsaturated-fat_100g":0.3,"polyunsaturated-fat_100g":0.4,"cholesterol_100g":0.005}}}""", "123")
        val cached = ProductJson.decode(ProductJson.encode(p))
        val n = food(cached).getJSONObject("per100")
        assertEquals(40.0, n.getDouble("sodium"), .00001)
        assertEquals(5.0, n.getDouble("cholesterol"), .00001)
        assertEquals(.2, n.getDouble("saturated"), .00001)
        assertEquals(.01, n.getDouble("trans"), .00001)
    }

    @Test fun `legacy cache stays readable and missing API names are not exported as barcode labels`() {
        val legacy = JSONObject(ProductJson.encode(product)).apply { remove("nameKnown") }
        assertTrue(ProductJson.decode(legacy.toString()).nameKnown)
        val noName = ProductJson.fromApi("""{"status":1,"product":{}}""", "123")
        assertFalse(noName.nameKnown)
    }
    @Test fun `values only adds the quick mode and keeps the same food`() {
        val full = JSONObject(GymManeImport.serialize(product))
        val quick = JSONObject(GymManeImport.serialize(product, quick = true))
        assertFalse(full.has("mode"))
        assertEquals("quick", quick.getString("mode"))
        assertEquals(full.getJSONObject("food").toString(), quick.getJSONObject("food").toString())
    }
}
