// AdmToken.kt
// Lokalizacja: app/src/main/java/com/example/oplatyogrodowelicz/data/AdmToken.kt
package com.example.oplatyogrodowelicz.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

// Token admina z login.php (brief: token admina).
// Wysyłany w nagłówku X-ADM-TOKEN przez ApiService.
object AdmToken {
    private const val PLIK = "adm_token"
    private const val KLUCZ = "token"
    private var prefs: SharedPreferences? = null

    private val _sesjaWygasla = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val sesjaWygasla: SharedFlow<Unit> = _sesjaWygasla.asSharedFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PLIK, Context.MODE_PRIVATE)
        }
    }

    fun zapisz(token: String) {
        prefs?.edit()?.putString(KLUCZ, token)?.apply()
    }

    fun pobierz(): String = prefs?.getString(KLUCZ, "") ?: ""

    fun usun() {
        prefs?.edit()?.remove(KLUCZ)?.apply()
    }

    /** Serwer odrzucił token (401 spoza logowania): kasuj i daj sygnał do powrotu na logowanie. */
    fun wygasla() {
        usun()
        _sesjaWygasla.tryEmit(Unit)
    }
}