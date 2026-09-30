// LoginViewModel.kt - zaktualizowany
// Lokalizacja: app/src/main/java/com/example/oplatyogrodowelicz/ui/screens/LoginViewModel.kt
package com.example.oplatyogrodowelicz.ui.screens

import androidx.compose.runtime.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oplatyogrodowelicz.data.network.ApiService
import com.example.oplatyogrodowelicz.ui.common.UiMessage
import com.example.oplatyogrodowelicz.ui.common.MessageMapper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val users: List<String> = emptyList(),
    val isLoadingUsers: Boolean = false,
    val isLogging: Boolean = false,
    val message: UiMessage? = null,
    val hasError: Boolean = false  // ← ZMIANA: zamiast errorMessage
)

class LoginViewModel : ViewModel() {
    private val apiService = ApiService()

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    // ← ZMIANA: Legacy properties dla backward compatibility (do usunięcia później)
    val users: List<String> get() = _uiState.value.users
    val isLoadingUsers: Boolean get() = _uiState.value.isLoadingUsers
    val isLogging: Boolean get() = _uiState.value.isLogging
    val errorMessage: String get() = when (val msg = _uiState.value.message) {
        is UiMessage.Error -> msg.text
        else -> ""
    }

    fun loadUsers(ogrodId: String) {
        // Cache - jeśli mamy dane i nie ma błędu, nie pobieraj
        if (_uiState.value.users.isNotEmpty() && !_uiState.value.hasError) {
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingUsers = true,
                message = null,
                hasError = false
            )

            try {
                val users = apiService.getUsers(ogrodId)
                _uiState.value = _uiState.value.copy(
                    users = users,
                    isLoadingUsers = false,
                    hasError = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoadingUsers = false,
                    hasError = true,
                    message = MessageMapper.mapErrorToUserFriendly(e.message ?: "Błąd pobierania użytkowników")
                )
            }
        }
    }

    fun login(
        ogrodId: String,
        username: String,
        password: String,
        rememberMe: Boolean,
        miasto: String,
        nazwaOgrodu: String,
        prefsManager: com.example.oplatyogrodowelicz.data.SharedPreferencesManager,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLogging = true,
                message = null
            )

            try {
                val success = apiService.login(ogrodId, username, password)
                if (success) {
                    prefsManager.saveLoginData(ogrodId, username, rememberMe, miasto, nazwaOgrodu)
                    _uiState.value = _uiState.value.copy(isLogging = false)
                    onSuccess()
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLogging = false,
                        message = MessageMapper.mapErrorToUserFriendly("Nieprawidłowe dane logowania")
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLogging = false,
                    message = MessageMapper.mapErrorToUserFriendly(e.message ?: "Nieznany błąd")
                )
            }
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}