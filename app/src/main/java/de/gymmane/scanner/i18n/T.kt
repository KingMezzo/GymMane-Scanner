package de.gymmane.scanner.i18n

import android.content.Context
import android.content.res.Configuration
import de.gymmane.scanner.R
import java.util.Locale

/** Resource-backed catalog; also localizes legacy validation messages at the UI boundary. */
object T {
    private var localized: Context? = null
    val isEnglish: Boolean get() = localized?.resources?.configuration?.locales?.get(0)?.language == "en"
    fun number(value: Double): String = de.gymmane.scanner.domain.formatNumber(value).let {
        if (isEnglish) it.replace(',', '.') else it
    }
    fun configure(context: Context, code: String) {
        val config = Configuration(context.resources.configuration)
        config.setLocale(Locale.forLanguageTag(code))
        localized = context.applicationContext.createConfigurationContext(config)
    }
    private val ids = mapOf(
        "Barcode scannen" to R.string.text_scan_barcode,
        "Werte prüfen" to R.string.text_check_values,
        "Barcode in den Rahmen halten" to R.string.text_place_the_barcode_inside_the_frame,
        "Produkt wird gesucht \u2026" to R.string.text_finding_product,
        "Erneut suchen" to R.string.text_try_again,
        "Weiter scannen" to R.string.text_continue_scanning,
        "Barcode eintippen" to R.string.text_enter_barcode,
        "Manuell eintragen" to R.string.text_add_manually,
        "Die 8, 12 oder 13 Ziffern unter dem Barcode eingeben." to R.string.text_enter_the_8_12_or_13_digits_below_the_barcode,
        "Produkt suchen" to R.string.text_find_product,
        "Abbrechen" to R.string.text_cancel,
        "Nicht angegeben" to R.string.text_not_available,
        "Kein Bild" to R.string.text_no_image,
        "Offline gespeichert" to R.string.text_saved_offline,
        "Name" to R.string.text_name,
        "Name kopieren" to R.string.text_copy_name,
        "pro Portion" to R.string.text_per_serving,
        "Eiwei\u00df" to R.string.text_protein,
        "Kohlenhydrate" to R.string.text_carbs,
        "Fett" to R.string.text_fat,
        "Weniger N\u00e4hrwerte" to R.string.text_fewer_nutrients,
        "Weitere N\u00e4hrwerte" to R.string.text_more_nutrients,
        "Ballaststoffe" to R.string.text_fiber,
        "Zucker" to R.string.text_sugar,
        "Natrium" to R.string.text_sodium,
        "Details schlie\u00dfen" to R.string.text_close_details,
        "Portion & Produktdetails" to R.string.text_serving_product_details,
        "Portion" to R.string.text_serving,
        "1 Portion" to R.string.text_one_serving,
        "1 Dose" to R.string.text_one_can,
        "1 Flasche" to R.string.text_one_bottle,
        "1 Becher" to R.string.text_one_pot,
        "1 Glas" to R.string.text_one_glass,
        "1 Packung" to R.string.text_one_pack,
        "Ohne Portionsmenge wird eine Bezugsportion von 100 g bzw. 100 ml geteilt." to R.string.text_reference_serving,
        "Gramm" to R.string.text_grams,
        "Milliliter" to R.string.text_milliliters,
        "Kategorie" to R.string.text_category,
        "Eintrag bearbeiten" to R.string.text_edit_food,
        "Wird aktualisiert \u2026" to R.string.text_updating,
        "Daten aktualisieren" to R.string.text_refresh_data,
        "Manueller Eintrag" to R.string.text_manual_entry,
        "An GymMane senden" to R.string.text_share_with_gymmane,
        "Name oder Barcode suchen" to R.string.text_search_name_or_barcode,
        "Suche leeren" to R.string.text_clear_search,
        "Dein Verlauf wartet auf dich" to R.string.text_no_saved_foods_yet,
        "Kein Treffer" to R.string.text_no_results,
        "Scanne dein erstes Produkt oder trage ein Lebensmittel selbst ein." to R.string.text_scan_a_product_or_add_a_food_manually,
        "Versuche einen anderen Namen oder Barcode." to R.string.text_try_another_name_or_barcode,
        "Erstes Produkt scannen" to R.string.text_scan_first_product,
        "Suche zur\u00fccksetzen" to R.string.text_clear_search_t042,
        "Manuell eingetragen" to R.string.text_added_manually,
        "Kalorien nicht angegeben" to R.string.text_calories_not_available,
        "Eintrag l\u00f6schen?" to R.string.text_delete_food,
        "L\u00f6schen" to R.string.text_delete,
        "Behalten" to R.string.text_keep,
        "Name *" to R.string.text_name_t048,
        "Angaben vom Etikett" to R.string.text_nutrition_label,
        "je 100 g" to R.string.text_per_100_g,
        "je 100 ml" to R.string.text_per_100_ml,
        "Kalorien *" to R.string.text_calories,
        "Eiwei\u00df *" to R.string.text_protein_t053,
        "Kohlenhydrate *" to R.string.text_carbs_t054,
        "Fett *" to R.string.text_fat_t055,
        "Weniger Angaben" to R.string.text_fewer_details,
        "Optionale Angaben" to R.string.text_optional_details,
        "Kategorie (optional)" to R.string.text_category_optional,
        "Barcode (optional)" to R.string.text_barcode_optional,
        "Portion (optional)" to R.string.text_serving_optional,
        "Bezeichnung, z. B. 1 Messl\u00f6ffel" to R.string.text_label_e_g_1_scoop,
        "Portionsgr\u00f6\u00dfe" to R.string.text_serving_size,
        "Weitere N\u00e4hrwerte (optional)" to R.string.text_additional_nutrients_optional,
        "Wird gespeichert \u2026" to R.string.text_saving,
        "Speichern und an GymMane senden" to R.string.text_save_and_share_with_gymmane,
        "Lebensmittel speichern" to R.string.text_save_food,
        "Zur\u00fcck zum Scanner" to R.string.text_back_to_scanner,
        "Lebensmittel" to R.string.text_food,
        "Deine Lebensmittel" to R.string.text_your_foods,
        "Neues Lebensmittel" to R.string.text_new_food,
        "Lebensmittel bearbeiten" to R.string.text_edit_food_t071,
        "Einstellungen" to R.string.text_settings,
        "Scannen" to R.string.text_scan,
        "Verlauf" to R.string.text_history,
        "Automatisch an GymMane senden nach Scan" to R.string.text_open_share_sheet_after_scanning,
        "Angaben f\u00fcr GymMane erg\u00e4nzen" to R.string.text_complete_food_details,
        "Werte bearbeiten" to R.string.text_edit_values,
        "KAMERA WIRD GESTARTET" to R.string.text_starting_camera,
        "BEREIT ZUM SCANNEN" to R.string.text_ready_to_scan,
        "SCAN PAUSIERT" to R.string.text_scan_paused,
        "Licht ausschalten" to R.string.text_turn_light_off,
        "Licht einschalten" to R.string.text_turn_light_on,
        "Barcode ausrichten \u00b7 zum Fokussieren tippen" to R.string.text_tap_to_focus,
        "Kamera freigeben" to R.string.text_allow_camera_access,
        "Kamera nicht verf\u00fcgbar" to R.string.text_camera_unavailable,
        "Die Kamera wird nur zum Lesen von Barcodes verwendet. Es werden keine Kamerabilder gespeichert oder hochgeladen." to R.string.text_camera_access_is_only_used_to_read_barcodes_camera_images_are_not,
        "Erneut versuchen" to R.string.text_try_again_t087,
        "Kamera erlauben" to R.string.text_allow_camera,
        "App-Einstellungen \u00f6ffnen" to R.string.text_open_app_settings,
        "Die Kamera ist gerade nicht verf\u00fcgbar. Schlie\u00dfe andere Kamera-Apps und versuche es erneut." to R.string.text_camera_unavailable_close_other_camera_apps_and_try_again,
        "Die Barcode-Erkennung konnte nicht gestartet werden. Bitte Kamera erneut \u00f6ffnen oder den Code eintippen." to R.string.text_barcode_recognition_could_not_start_reopen_the_camera_or_enter_th,
        "Die Kamera konnte nicht ge\u00f6ffnet werden. Pr\u00fcfe, ob eine andere App sie verwendet." to R.string.text_could_not_open_the_camera_check_whether_another_app_is_using_it,
        "Der lokale Verlauf konnte nicht gelesen werden." to R.string.text_could_not_read_saved_foods,
        "Bitte einen g\u00fcltigen EAN-8-, EAN-13- oder UPC-A-Code mit korrekter Pr\u00fcfziffer eingeben." to R.string.text_enter_a_valid_ean_8_ean_13_or_upc_a_barcode_with_a_correct_check_,
        "Keine Verbindung zur Produktdatenbank. Pr\u00fcfe deine Internetverbindung. Gespeicherte Produkte findest du im Verlauf." to R.string.text_cannot_connect_to_the_product_database_check_your_connection_or_o,
        "Das Produkt konnte nicht geladen oder gespeichert werden. Bitte erneut versuchen." to R.string.text_could_not_load_or_save_the_product_please_try_again,
        "Der Verlauf konnte nicht aktualisiert werden." to R.string.text_could_not_update_saved_foods,
        "Speichern fehlgeschlagen. Bitte freien Ger\u00e4tespeicher pr\u00fcfen." to R.string.text_could_not_save_check_available_device_storage,
        "Das Produkt konnte nicht gel\u00f6scht werden." to R.string.text_could_not_delete_the_food,
        "Die Produktdaten konnten nicht gelesen werden." to R.string.text_could_not_read_product_data,
        "Dieses Produkt ist bei Open Food Facts noch nicht hinterlegt." to R.string.text_this_product_is_not_available_on_open_food_facts_yet,
        "Zu viele Anfragen. Bitte warte eine Minute und versuche es erneut." to R.string.text_too_many_requests_wait_a_minute_and_try_again,
        "Bitte gib einen Namen ein." to R.string.text_enter_a_name,
        "Der Barcode ist ung\u00fcltig. Pr\u00fcfe Ziffern und Pr\u00fcfziffer oder lasse das Feld leer." to R.string.text_invalid_barcode_check_the_digits_and_check_digit_or_leave_it_empt,
        "Die Portionsgr\u00f6\u00dfe muss gr\u00f6\u00dfer als 0 sein." to R.string.text_serving_size_must_be_greater_than_0,
        "Bitte Kalorien, Eiwei\u00df, Kohlenhydrate und Fett als g\u00fcltige Zahlen eintragen. 0 ist erlaubt." to R.string.text_enter_valid_numbers_for_calories_protein_carbs_and_fat_zero_is_al,
        "Bitte optionale N\u00e4hrwerte als g\u00fcltige Zahlen eintragen oder leer lassen." to R.string.text_enter_valid_numbers_for_optional_nutrients_or_leave_them_empty,
        "Bitte die Werte je 100 g / ml pr\u00fcfen: h\u00f6chstens 1000 kcal und 100 g je N\u00e4hrstoff." to R.string.text_check_the_values_per_100_g_ml_at_most_1000_kcal_and_100_g_per_nut,
        "Kalorien" to R.string.text_calories_t109,
        "Portionsgewicht" to R.string.text_serving_weight,
        "%1\$s gespeicherte Lebensmittel" to R.string.text_saved_foods,
        "%1\$s kopieren" to R.string.text_copy,
        "%1\$s kopiert: %2\$s" to R.string.text_copied,
        "Produktbild: %1\$s" to R.string.text_product_image,
        "Portionsgr\u00f6\u00dfe in %1\$s" to R.string.text_serving_size_in,
        "%1\$s aus Verlauf l\u00f6schen" to R.string.text_delete_from_history,
        "\u201e%1\$s\u201c wird mit deinen manuell eingegebenen Werten von diesem Ger\u00e4t entfernt." to R.string.text_and_its_manually_entered_values_will_be_removed_from_this_device,
        "\u201e%1\$s\u201c wird aus dem Offline-Verlauf entfernt. Du kannst es sp\u00e4ter erneut scannen." to R.string.text_will_be_removed_from_history_you_can_scan_it_again_later,
        "Auch die optionalen Werte gelten je 100 %1\$s. Natrium wird in Gramm erfasst, nicht in Milligramm. Leere optionale Felder bleiben unbekannt." to R.string.text_optional_values_are_also_per_100_enter_sodium_in_grams_not_millig,
        "Bitte vor dem Senden erg\u00e4nzen oder korrigieren: %1\$s. Alle N\u00e4hrwerte gelten je 100 g bzw. 100 ml." to R.string.text_complete_or_correct_before_sharing_all_values_are_per_100_g_or_10,
        "Open Food Facts ist gerade nicht erreichbar (HTTP %1\$s). Bitte sp\u00e4ter erneut versuchen." to R.string.text_open_food_facts_is_unavailable_http_please_try_again_later,
        "Willkommen" to R.string.text_welcome,
        "Scannen. Pr\u00fcfen. Teilen." to R.string.text_scan_check_share,
        "Finde N\u00e4hrwerte per Barcode und teile das Lebensmittel direkt mit GymMane." to R.string.text_find_nutrition_by_barcode_and_share_the_food_directly_with_gymman,
        "Sprache" to R.string.text_language,
        "Loslegen" to R.string.text_get_started,
        "Teilen" to R.string.text_sharing,
        "Nach dem Scan \u00f6ffnet sich der Teilen-Dialog. W\u00e4hle GymMane zum Einf\u00fcgen." to R.string.text_the_share_sheet_opens_after_scanning_choose_gymmane_to_add_the_fo,
        "App-Version" to R.string.text_app_version,
        "Quellcode" to R.string.text_source_code,
        "Repository-Link noch nicht hinterlegt" to R.string.text_repository_link_not_configured_yet,
        "Onboarding erneut ansehen" to R.string.text_view_introduction,
        "Datenschutz & Datenquelle" to R.string.text_privacy_data_source,
        "Barcodes werden bei Open Food Facts gesucht. Verlauf und manuelle Eintr\u00e4ge bleiben auf deinem Ger\u00e4t. Kamerabilder werden nicht hochgeladen." to R.string.text_barcodes_are_looked_up_on_open_food_facts_history_and_manual_entr,
        "Produktdaten: Open Food Facts \u00b7 ODbL. Bilder: CC BY-SA." to R.string.text_product_data_open_food_facts_odbl_images_cc_by_sa,
        "Das Lebensmittel konnte nicht geteilt werden. Bitte erneut versuchen." to R.string.text_could_not_share_this_food_please_try_again,
        "Mit GymMane teilen" to R.string.text_share_with_gymmane_t137,
        "Die letzten 100 gescannten Produkte und alle manuellen Einträge werden lokal gespeichert. Tippe auf ein Produkt, um seine Werte zu kopieren." to R.string.text_history_note,
        "Nur Nährwerte senden" to R.string.text_send_values_only
    )
    fun format(key: String, vararg args: Any): String {
        val resource = ids[key]
        return if (localized != null && resource != null) localized!!.getString(resource, *args)
        else String.format(Locale.GERMAN, key, *args)
    }
    fun text(value: String): String {
        ids[value]?.let { return localized?.getString(it) ?: value }
        if (localized?.resources?.configuration?.locales?.get(0)?.language != "en") return value
        for ((template, id) in ids) {
            if (!template.contains("%1\$s")) continue
            val parts = template.split(Regex("%[0-9]+[" + '$' + "]s"))
            val pattern = parts.joinToString("(.*?)") { Regex.escape(it) }
            val match = Regex("^" + pattern + "$", RegexOption.DOT_MATCHES_ALL).matchEntire(value) ?: continue
            val args = match.groupValues.drop(1).map { part ->
                if (template.startsWith("Bitte vor")) part.split(", ").joinToString(", ") { text(it) } else part
            }.toTypedArray()
            return localized!!.getString(id, *args)
        }
        return value
    }
}
