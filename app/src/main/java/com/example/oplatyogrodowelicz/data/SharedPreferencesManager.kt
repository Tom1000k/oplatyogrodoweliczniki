// SharedPreferencesManager.kt
// Lokalizacja: app/src/main/java/com/example/oplatyogrodoweladm/data/SharedPreferencesManager.kt
package com.example.oplatyogrodowelicz.data

import android.content.Context
import android.content.SharedPreferences

class SharedPreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_REMEMBER_ME = "remember_me"
        private const val KEY_OGROD_ID = "ogrod_id"
        private const val KEY_USERNAME = "username"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_MIASTO = "miasto"
        private const val KEY_NAZWA_OGRODU = "nazwa_ogrodu"
    }

    // Data class dla zwracanych danych
    data class SavedLoginData(
        val ogrodId: String?,
        val username: String?,
        val rememberMe: Boolean,
        val miasto: String?,
        val nazwaOgrodu: String?
    )

    fun saveLoginData(ogrodId: String, username: String, rememberMe: Boolean, miasto: String = "", nazwaOgrodu: String = "") {
        sharedPreferences.edit().apply {
            putString(KEY_OGROD_ID, ogrodId)
            putString(KEY_USERNAME, username)
            putBoolean(KEY_REMEMBER_ME, rememberMe)
            putBoolean(KEY_IS_LOGGED_IN, rememberMe)
            putString(KEY_MIASTO, miasto)
            putString(KEY_NAZWA_OGRODU, nazwaOgrodu)
            apply()
        }
    }

    // Stara metoda dla kompatybilności wstecznej
    fun getSavedLoginData(): Triple<String?, String?, Boolean> {
        val ogrodId = sharedPreferences.getString(KEY_OGROD_ID, null)
        val username = sharedPreferences.getString(KEY_USERNAME, null)
        val rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false)
        return Triple(ogrodId, username, rememberMe)
    }

    // Nowa metoda z wszystkimi danymi
    fun getSavedLoginDataFull(): SavedLoginData {
        return SavedLoginData(
            ogrodId = sharedPreferences.getString(KEY_OGROD_ID, null),
            username = sharedPreferences.getString(KEY_USERNAME, null),
            rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false),
            miasto = sharedPreferences.getString(KEY_MIASTO, null),
            nazwaOgrodu = sharedPreferences.getString(KEY_NAZWA_OGRODU, null)
        )
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun clearLoginData() {
        sharedPreferences.edit().apply {
            remove(KEY_OGROD_ID)
            remove(KEY_USERNAME)
            remove(KEY_REMEMBER_ME)
            remove(KEY_IS_LOGGED_IN)
            remove(KEY_MIASTO)
            remove(KEY_NAZWA_OGRODU)
            apply()
        }
    }

    fun logout() {
        clearLoginData()
    }
}