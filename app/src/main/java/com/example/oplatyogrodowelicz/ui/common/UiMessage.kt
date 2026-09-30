// UiMessage.kt
// Lokalizacja: app/src/main/java/com/example/oplatyogrodowelicz/ui/common/UiMessage.kt
package com.example.oplatyogrodowelicz.ui.common

import androidx.compose.material3.SnackbarDuration

sealed class UiMessage(
    val text: String,
    val duration: SnackbarDuration = SnackbarDuration.Short,
    val actionLabel: String? = null
) {
    // Błędy - czerwone, dłużej wyświetlane
    class Error(
        text: String,
        actionLabel: String? = "OK"
    ) : UiMessage(text, SnackbarDuration.Long, actionLabel)

    // Sukces - zielone, krótko
    class Success(
        text: String
    ) : UiMessage(text, SnackbarDuration.Short)

    // Info - niebieskie, średnio
    class Info(
        text: String,
        actionLabel: String? = null
    ) : UiMessage(text, SnackbarDuration.Short, actionLabel)

    // Ostrzeżenie - pomarańczowe, dłużej
    class Warning(
        text: String,
        actionLabel: String? = "Rozumiem"
    ) : UiMessage(text, SnackbarDuration.Long, actionLabel)
}