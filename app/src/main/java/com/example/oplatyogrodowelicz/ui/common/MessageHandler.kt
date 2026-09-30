// MessageHandler.kt
// Lokalizacja: app/src/main/java/com/example/oplatyogrodowelicz/ui/common/MessageHandler.kt
package com.example.oplatyogrodowelicz.ui.common

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect

@Composable
fun HandleUiMessage(
    message: UiMessage?,
    snackbarHostState: SnackbarHostState,
    onMessageShown: () -> Unit
) {
    message?.let { msg ->
        LaunchedEffect(msg) {
            snackbarHostState.showSnackbar(
                message = msg.text,
                actionLabel = msg.actionLabel,
                duration = msg.duration
            )
            onMessageShown()
        }
    }
}