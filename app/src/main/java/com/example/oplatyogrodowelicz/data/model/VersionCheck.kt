// VersionCheck.kt
// Lokalizacja: app/src/main/java/com/example/oplatyogrodoweadm/data/model/VersionCheck.kt
package com.example.oplatyogrodowelicz.data.model

data class VersionCheckResponse(
    val appName: String,
    val currentVersion: String,
    val latestVersion: String,
    val recommendedVersion: String,
    val minimumVersion: String,
    val needsUpdate: Boolean,
    val mustUpdate: Boolean,
    val updateAvailable: Boolean,
    val changelog: String
)

enum class UpdateType {
    NONE,        // Brak aktualizacji
    OPTIONAL,    // Opcjonalna aktualizacja (można odrzucić)
    REQUIRED     // Wymagana aktualizacja (blokada aplikacji)
}