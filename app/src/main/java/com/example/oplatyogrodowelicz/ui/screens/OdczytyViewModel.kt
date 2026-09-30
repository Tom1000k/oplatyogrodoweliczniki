// OdczytyViewModel.kt - zaktualizowany
// Lokalizacja: app/src/main/java/com/example/oplatyogrodowelicz/ui/screens/OdczytyViewModel.kt
package com.example.oplatyogrodowelicz.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oplatyogrodowelicz.data.model.*
import com.example.oplatyogrodowelicz.data.network.ApiService
import com.example.oplatyogrodowelicz.ui.common.UiMessage
import com.example.oplatyogrodowelicz.ui.common.MessageMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class OdczytyUiState(
    val isLoading: Boolean = false,
    val message: UiMessage? = null,  // ← ZMIANA: zamiast errorMessage i successMessage
    val tereny: List<Teren> = emptyList(),
    val sektory: List<Sektor> = emptyList(),
    val selectedTypLicznika: String = "prad",
    val selectedTerenId: String? = null,
    val selectedSektorId: String? = null,
    val odczyty: List<Odczyt> = emptyList(),
    val showOdczyty: Boolean = false,
    val zapisaneLiczniki: Set<String> = emptySet(),
    val zaktualizowaneLiczniki: Set<String> = emptySet()
)

class OdczytyViewModel : ViewModel() {
    private val apiService = ApiService()

    private val _uiState = MutableStateFlow(OdczytyUiState())
    val uiState: StateFlow<OdczytyUiState> = _uiState

    fun loadFiltersData(ogrodId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)

            try {
                val dzialkiData = apiService.pobierzDzialki(ogrodId)

                val tereny = when (dzialkiData) {
                    is DzialkiData.Single -> emptyList()
                    is DzialkiData.Multi -> dzialkiData.terenyZDzialkami.map { it.teren }
                }

                val sektory = when (dzialkiData) {
                    is DzialkiData.Single -> dzialkiData.sektory
                    is DzialkiData.Multi -> dzialkiData.sektory
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    tereny = tereny,
                    sektory = sektory
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = MessageMapper.mapErrorToUserFriendly(e.message ?: "Błąd ładowania danych")
                )
            }
        }
    }

    fun setTypLicznika(typ: String) {
        _uiState.value = _uiState.value.copy(
            selectedTypLicznika = typ,
            showOdczyty = false,
            odczyty = emptyList()
        )
    }

    fun setSelectedTeren(terenId: String?) {
        _uiState.value = _uiState.value.copy(
            selectedTerenId = terenId,
            selectedSektorId = null,
            showOdczyty = false,
            odczyty = emptyList()
        )
    }

    fun setSelectedSektor(sektorId: String?) {
        _uiState.value = _uiState.value.copy(
            selectedSektorId = sektorId,
            selectedTerenId = null,
            showOdczyty = false,
            odczyty = emptyList()
        )
    }

    fun wyswietlOdczyty(ogrodId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, message = null)

            try {
                val odczyty = apiService.pobierzOdczyty(
                    ogrodId = ogrodId,
                    idsektory = _uiState.value.selectedSektorId,
                    terenId = _uiState.value.selectedTerenId,
                    medium = _uiState.value.selectedTypLicznika
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    odczyty = odczyty,
                    showOdczyty = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    message = MessageMapper.mapErrorToUserFriendly(e.message ?: "Błąd pobierania odczytów")
                )
            }
        }
    }

    fun zapiszOdczyt(ogrodId: String, idlicznika: String, nowyOdczyt: String) {
        viewModelScope.launch {
            // Sprawdź czy to ogród DEMO
            if (ogrodId.equals("DEMO", ignoreCase = true)) {
                _uiState.value = _uiState.value.copy(
                    message = MessageMapper.mapErrorToUserFriendly("W ogrodzie DEMO nie dokonujemy zapisów")
                )
                return@launch
            }

            try {
                val (success, wasUpdate) = apiService.zapiszOdczyt(
                    ogrodId = ogrodId,
                    idlicznika = idlicznika,
                    nowyOdczyt = nowyOdczyt
                )

                if (success) {
                    val successMessage = if (wasUpdate) "Odczyt zaktualizowany" else "Odczyt zapisany"

                    _uiState.value = _uiState.value.copy(
                        zapisaneLiczniki = _uiState.value.zapisaneLiczniki + idlicznika,
                        zaktualizowaneLiczniki = if (wasUpdate) {
                            _uiState.value.zaktualizowaneLiczniki + idlicznika
                        } else {
                            _uiState.value.zaktualizowaneLiczniki
                        },
                        message = MessageMapper.mapSuccessMessage(successMessage)
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    message = MessageMapper.mapErrorToUserFriendly(e.message ?: "Błąd zapisu odczytu")
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }

    fun isLicznikZapisany(idlicznika: String): Boolean {
        return _uiState.value.zapisaneLiczniki.contains(idlicznika)
    }

    fun isLicznikZaktualizowany(idlicznika: String): Boolean {
        return _uiState.value.zaktualizowaneLiczniki.contains(idlicznika)
    }
}