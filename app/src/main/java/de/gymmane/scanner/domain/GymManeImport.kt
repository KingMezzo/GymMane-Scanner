package de.gymmane.scanner.domain

import org.json.JSONObject

class IncompleteGymManeFood(val fields: List<String>) : IllegalArgumentException(
    "Bitte vor dem Senden ergänzen oder korrigieren: ${fields.joinToString(", ")}. Alle Nährwerte gelten je 100 g bzw. 100 ml."
)

/** Deterministic import serialization. Internal mass nutrients are grams; export Na/Chol are mg. */
object GymManeImport {
    fun foodName(product: Product): String {
        val name = product.name.trim()
        val missingBrands = product.brand.orEmpty().split(',').map(String::trim).filter(String::isNotBlank)
            .distinctBy { it.lowercase(java.util.Locale.ROOT) }.filterNot { name.contains(it, ignoreCase = true) }
        return (listOf(name) + missingBrands).filter(String::isNotBlank).joinToString(" · ")
    }

    fun serialize(product: Product, quick: Boolean = false): String {
        val n = product.per100
        fun Double?.known() = this != null && isFinite() && this >= 0
        val missing = buildList {
            if (!product.nameKnown || product.name.isBlank()) add("Name")
            if (product.servingAmount != null && (!product.servingAmount.isFinite() || product.servingAmount <= 0)) add("Portionsgewicht")
            if (!n.kcal.known()) add("Kalorien")
            if (!n.protein.known()) add("Eiweiß")
            if (!n.carbs.known()) add("Kohlenhydrate")
            if (!n.fat.known()) add("Fett")
        }
        if (missing.isNotEmpty()) throw IncompleteGymManeFood(missing)
        val per100 = JSONObject().apply {
            put("kcal", n.kcal); put("protein", n.protein); put("carbs", n.carbs); put("fat", n.fat)
            fun optional(key: String, value: Double?) { if (value.known()) put(key, value) }
            optional("fiber", n.fiber); optional("sugar", n.sugar); optional("saturated", n.saturated)
            optional("trans", n.trans); optional("monounsaturated", n.monounsaturated)
            optional("polyunsaturated", n.polyunsaturated)
            optional("sodium", if (n.sodium.known()) n.sodium!! * 1000 else n.salt?.takeIf { it.known() }?.times(400))
            optional("cholesterol", n.cholesterol?.takeIf { it.known() }?.times(1000))
        }
        val amount = product.servingAmount ?: 100.0
        val serving = Serving.name(product)
        return JSONObject().put("gymmane", "gymmane-food").put("v", 1).apply { if (quick) put("mode", "quick") }.put("food", JSONObject()
            .put("name", foodName(product)).put("serving_name", serving).put("grams", amount)
            .put("liquid", product.unit == QuantityUnit.ML).put("per100", per100)).toString()
    }
}
