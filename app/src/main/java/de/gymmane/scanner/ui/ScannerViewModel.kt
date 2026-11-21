package de.gymmane.scanner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import de.gymmane.scanner.data.*
import de.gymmane.scanner.domain.Barcode
import de.gymmane.scanner.domain.Product
import de.gymmane.scanner.domain.GymManeImport
import de.gymmane.scanner.domain.IncompleteGymManeFood
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.IOException

enum class Screen { SCAN, HISTORY, PRODUCT, MANUAL, SETTINGS }
data class PendingGymManeSend(val id: Long, val json: String)
data class UiState(
    val screen: Screen = Screen.SCAN,
    val product: Product? = null,
    val history: List<Product> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val lastBarcode: String = "",
    val cached: Boolean = false,
    val editing: Product? = null,
    val autoSend: Boolean = false,
    val pendingSend: PendingGymManeSend? = null,
    val correction: Product? = null,
    val correctionError: String? = null,
    val editingForSend: Boolean = false,
)

class ScannerViewModel(private val repository: ProductStore, private val settings: AutoSendSettings) : ViewModel() {
    private val mutableState = MutableStateFlow(UiState(autoSend = settings.autoSend))
    val state = mutableState.asStateFlow()
    private var lookupJob: Job? = null
    private var nextSendId = 0L
    init { loadHistory() }

    private fun loadHistory() = viewModelScope.launch {
        try { mutableState.update { it.copy(history = repository.history()) } }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { mutableState.update { it.copy(error = "Der lokale Verlauf konnte nicht gelesen werden.") } }
    }

    fun navigate(screen: Screen) {
        lookupJob?.cancel()
        mutableState.update { it.copy(screen = screen, loading = false, error = null, editing = null,
            editingForSend = false, pendingSend = null, correction = null, correctionError = null) }
    }

    fun lookup(value: String, refresh: Boolean = false) {
        if (state.value.loading) return
        val afterScan = state.value.screen == Screen.SCAN && !refresh
        val barcode = Barcode.normalize(value)
        if (barcode == null) {
            mutableState.update { it.copy(lastBarcode = "", error = "Bitte einen gültigen EAN-8-, EAN-13- oder UPC-A-Code mit korrekter Prüfziffer eingeben.") }
            return
        }
        mutableState.update { it.copy(loading = true, error = null, lastBarcode = barcode) }
        lookupJob = viewModelScope.launch {
            try {
                val result = repository.lookup(barcode, refresh)
                mutableState.update { it.copy(product = result.product, cached = result.cached, loading = false, screen = Screen.PRODUCT) }
                loadHistory()
                if (afterScan && state.value.autoSend) send(result.product)
            } catch (e: CancellationException) { throw e }
            catch (e: Exception) {
                val message = when (e) {
                    is ProductNotFound, is ProductServiceException, is InvalidProductResponse -> e.message!!
                    is IOException -> "Keine Verbindung zur Produktdatenbank. Prüfe deine Internetverbindung. Gespeicherte Produkte findest du im Verlauf."
                    else -> "Das Produkt konnte nicht geladen oder gespeichert werden. Bitte erneut versuchen."
                }
                mutableState.update { it.copy(loading = false, error = message) }
            }
        }
    }

    fun open(product: Product) {
        mutableState.update { it.copy(product = product, screen = Screen.PRODUCT, cached = true, error = null,
            pendingSend = null, correction = null, correctionError = null, editingForSend = false) }
        viewModelScope.launch {
            try { repository.save(product); loadHistory() }
            catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutableState.update { it.copy(error = "Der Verlauf konnte nicht aktualisiert werden.") } }
        }
    }

    fun manual(edit: Product? = null) {
        lookupJob?.cancel()
        mutableState.update { it.copy(screen = Screen.MANUAL, editing = edit,
            lastBarcode = if (edit == null && it.error != null) it.lastBarcode else "", error = null, loading = false,
            editingForSend = false, pendingSend = null, correction = null, correctionError = null) }
    }

    fun save(product: Product) {
        if (state.value.loading) return
        val sendAfterSave = state.value.editingForSend
        mutableState.update { it.copy(loading = true, error = null) }
        lookupJob = viewModelScope.launch {
            try {
                repository.save(product)
                mutableState.update { it.copy(product = product, cached = true, screen = Screen.PRODUCT, loading = false, editing = null, editingForSend = false) }
                loadHistory()
                if (sendAfterSave) send(product)
            } catch (e: CancellationException) { throw e }
            catch (_: Exception) { mutableState.update { it.copy(loading = false, error = "Speichern fehlgeschlagen. Bitte freien Gerätespeicher prüfen.") } }
        }
    }

    fun delete(product: Product) = viewModelScope.launch {
        try { repository.delete(product); loadHistory() }
        catch (e: CancellationException) { throw e }
        catch (_: Exception) { mutableState.update { it.copy(error = "Das Produkt konnte nicht gelöscht werden.") } }
    }

    fun clearError() { mutableState.update { it.copy(error = null) } }

    fun setAutoSend(enabled: Boolean) {
        settings.autoSend = enabled
        mutableState.update { it.copy(autoSend = enabled) }
    }

    fun send(product: Product, quick: Boolean = false) {
        try {
            val json = GymManeImport.serialize(product, quick)
            val request = PendingGymManeSend(++nextSendId, json)
            mutableState.update { it.copy(pendingSend = request, error = null,
                correction = null, correctionError = null) }
        } catch (e: IncompleteGymManeFood) {
            mutableState.update { it.copy(correction = product, correctionError = e.message, pendingSend = null) }
        }
    }

    /** Consume before invoking Android, so rotation or returning from GymMane cannot resend. */
    fun consumeSend(id: Long): PendingGymManeSend? {
        val pending = state.value.pendingSend?.takeIf { it.id == id } ?: return null
        mutableState.update { it.copy(pendingSend = null) }
        return pending
    }

    fun sendFailed(message: String) { mutableState.update { it.copy(error = message) } }
    fun dismissCorrection() { mutableState.update { it.copy(correction = null, correctionError = null) } }
    fun editForSending() {
        val product = state.value.correction ?: return
        mutableState.update { it.copy(screen = Screen.MANUAL, editing = product, editingForSend = true,
            error = it.correctionError, correction = null, correctionError = null) }
    }

    class Factory(private val repository: ProductRepository, private val settings: ScannerSettings) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = ScannerViewModel(repository, settings) as T
    }
}
