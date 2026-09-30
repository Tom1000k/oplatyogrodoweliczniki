// OgrodyViewModel.kt - zaktualizowany
// Lokalizacja: app/src/main/java/com/example/oplatyogrodowelicz/ui/screens/OgrodyViewModel.kt
package com.example.oplatyogrodowelicz.ui.screens

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oplatyogrodowelicz.data.model.MiastoZOgrodami
import com.example.oplatyogrodowelicz.data.network.ApiService
import com.example.oplatyogrodowelicz.ui.common.UiMessage
import com.example.oplatyogrodowelicz.ui.common.MessageMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class OgrodyUiState(
    val miastaList: List<MiastoZOgrodami> = emptyList(),
    val isLoading: Boolean = false,
    val message: UiMessage? = null ,
    val hasError: Boolean = false
)

class OgrodyViewModel : ViewModel() {
    private val apiService = ApiService()

    private val _uiState = MutableStateFlow(OgrodyUiState())
    val uiState: StateFlow<OgrodyUiState> = _uiState

    // ← ZMIANA: Legacy properties dla backward compatibility (do usunięcia później)
    val miastaList: List<MiastoZOgrodami> get() = _uiState.value.miastaList
    val isLoading: Boolean get() = _uiState.value.isLoading
    val errorMessage: String get() = when (val msg = _uiState.value.message) {
        is UiMessage.Error -> msg.text
        else -> ""
    }

    fun loadMiasta() {
        if (_uiState.value.miastaList.isNotEmpty()) {
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                message = null,
                hasError = false
            )

            try {
                val miasta = apiService.pobierzMiastaZOgrodami()
                _uiState.value = _uiState.value.copy(
                    miastaList = miasta,
                    isLoading = false,
                    hasError = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    miastaList = emptyList(),
                    isLoading = false,
                    hasError = true,
                    message = MessageMapper.mapErrorToUserFriendly(e.message ?: "Błąd pobierania danych")
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}