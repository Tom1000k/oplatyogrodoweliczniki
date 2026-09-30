// MainActivity.kt
// Lokalizacja: app/src/main/java/com/example/smsapp/MainActivity.kt
package com.example.oplatyogrodowelicz

import android.app.AlertDialog
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.oplatyogrodowelicz.ui.screens.OgrodyScreen
import com.example.oplatyogrodowelicz.ui.screens.LoginScreen
import com.example.oplatyogrodowelicz.ui.theme.SMSAppTheme
import com.example.oplatyogrodowelicz.data.SharedPreferencesManager
import java.net.URLDecoder
import com.example.oplatyogrodowelicz.ui.screens.LicznikiScreen
import com.example.oplatyogrodowelicz.ui.screens.OdczytyLicznikowScreen
import androidx.core.content.edit
import com.example.oplatyogrodowelicz.data.model.VersionCheckResponse
import android.content.Intent
import android.content.ActivityNotFoundException
import android.net.Uri
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.oplatyogrodowelicz.data.network.ApiService
import java.net.URLEncoder

class MainActivity : ComponentActivity() {

    private val apiService = ApiService()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAppVersion()

        setContent {
            SMSAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SMSApp()
                }
            }
        }
    }
    private var currentOgrodId: String? = null

    // Dodaj metodę do ustawiania ID ogrodu
    fun setCurrentOgrodId(ogrodId: String) {
        currentOgrodId = ogrodId
    }

    override fun onDestroy() {
        super.onDestroy()
        currentOgrodId?.let { ogrodId ->
            val sharedPrefs = getSharedPreferences("dzialka_prefs_$ogrodId", MODE_PRIVATE)
            sharedPrefs.edit { clear() }
        }
    }


    private fun checkAppVersion() {
        lifecycleScope.launch {
            try {
                val currentVersion = getCurrentVersion()
                val response = apiService.sprawdzWersje("OPLATY_GOS", currentVersion)

                when {
                    response.mustUpdate -> showRequiredUpdateDialog(response)
                    response.needsUpdate && !wasUpdateSkipped(response.recommendedVersion) -> showOptionalUpdateDialog(response)
                }

            } catch (e: Exception) {
                // Ignoruj błędy - nie blokuj aplikacji
            }
        }
    }

    private fun getCurrentVersion(): String {
        return try {
            packageManager.getPackageInfo(packageName, 0).versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }
    }

    private fun showRequiredUpdateDialog(response: VersionCheckResponse) {
        val message = buildString {
            append("Twoja wersja (${response.currentVersion}) jest nieaktualna.\n")
            append("Minimalna wymagana: ${response.minimumVersion}")

            // ✅ DODAJ CHANGELOG JAK W OPCJONALNEJ
            if (response.changelog.isNotBlank()) {
                append("\nPowód:\n${response.changelog}")
            }
        }

        AlertDialog.Builder(this)
            .setTitle("Wymagana aktualizacja")
            .setMessage(message)
            .setPositiveButton("Aktualizuj") { _, _ -> openPlayStore() }
            .setNegativeButton("Zamknij") { _, _ -> finish() }
            .setCancelable(false)
            .show()
    }

    private fun showOptionalUpdateDialog(response: VersionCheckResponse) {
        AlertDialog.Builder(this)
            .setTitle("Dostępna aktualizacja")
            .setMessage("Dostępna nowa wersja: ${response.latestVersion}\n${response.changelog}")
            .setPositiveButton("Aktualizuj") { _, _ -> openPlayStore() }
            .setNegativeButton("Pomiń") { _, _ -> markUpdateSkipped(response.recommendedVersion) }
            .show()
    }

    private fun openPlayStore() {
        try {
            // Próbuj otworzyć aplikację Google Play
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("market://details?id=$packageName")
                setPackage("com.android.vending")
            }
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Jeśli brak Google Play, otwórz w przeglądarce
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
            }
            startActivity(intent)
        }
        finish() // Zamknij aplikację po otwarciu sklepu
    }

    private fun wasUpdateSkipped(version: String): Boolean {
        val prefs = getSharedPreferences("version_prefs", MODE_PRIVATE)
        return prefs.getString("skipped_version", null) == version
    }

    private fun markUpdateSkipped(version: String) {
        getSharedPreferences("version_prefs", MODE_PRIVATE)
            .edit().putString("skipped_version", version).apply()
    }
}


@Composable
fun SMSApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val prefsManager = remember { SharedPreferencesManager(context) }

    // Sprawdź autologowanie po starcie
    LaunchedEffect(Unit) {
        val savedData = prefsManager.getSavedLoginDataFull()
        if (savedData.rememberMe && !savedData.ogrodId.isNullOrEmpty() &&
            !savedData.miasto.isNullOrEmpty() && !savedData.nazwaOgrodu.isNullOrEmpty()) {
            val encodedMiasto = URLEncoder.encode(savedData.miasto, "UTF-8")
            val encodedNazwa = URLEncoder.encode(savedData.nazwaOgrodu, "UTF-8")
            navController.navigate("login/${savedData.ogrodId}/$encodedMiasto/$encodedNazwa")
        }
    }

    NavHost(
        navController = navController,
        startDestination = "ogrody"  // Zawsze startuj od ogrody
    ) {
        // Ekran wyboru ogrodu
        composable("ogrody") {
            OgrodyScreen(
                onOgrodSelected = { ogrod, miasto ->
                    // Przejdź do logowania z ID ogrodu, miastem i nazwą
                    val encodedMiasto = URLEncoder.encode(miasto, "UTF-8")
                    val encodedNazwa = URLEncoder.encode(ogrod.nazwa, "UTF-8")
                    navController.navigate("login/${ogrod.id}/$encodedMiasto/$encodedNazwa")
                }
            )
        }

        // Ekran logowania
        composable("login/{ogrodId}/{miasto}/{nazwaOgrodu}") { backStackEntry ->
            val ogrodId = backStackEntry.arguments?.getString("ogrodId") ?: ""
            val miasto = URLDecoder.decode(backStackEntry.arguments?.getString("miasto") ?: "", "UTF-8")
            val nazwaOgrodu = URLDecoder.decode(backStackEntry.arguments?.getString("nazwaOgrodu") ?: "", "UTF-8")

            LoginScreen(
                ogrodId = ogrodId,
                miasto = miasto,
                nazwaOgrodu = nazwaOgrodu,
                onLoginSuccess = {
                    val encodedMiasto = URLEncoder.encode(miasto, "UTF-8")
                    val encodedNazwa = URLEncoder.encode(nazwaOgrodu, "UTF-8")
                    navController.navigate("liczniki/$ogrodId/$encodedMiasto/$encodedNazwa") {
                        popUpTo("login/$ogrodId/$encodedMiasto/$encodedNazwa") { inclusive = true }
                    }
                },
                onBackPressed = {
                    navController.popBackStack()
                }
            )
        }

        composable("liczniki/{ogrodId}/{miasto}/{nazwaOgrodu}") { backStackEntry ->
            val ogrodId = backStackEntry.arguments?.getString("ogrodId") ?: ""
            val miasto = URLDecoder.decode(backStackEntry.arguments?.getString("miasto") ?: "", "UTF-8")
            val nazwaOgrodu = URLDecoder.decode(backStackEntry.arguments?.getString("nazwaOgrodu") ?: "", "UTF-8")

            LicznikiScreen(
                ogrodId = ogrodId,
                miasto = miasto,
                nazwaOgrodu = nazwaOgrodu,
                onBackPressed = {
                    navController.popBackStack()
                },
                onLogoutClicked = {
                    navController.navigate("ogrody") {
                        popUpTo("ogrody") { inclusive = true }
                    }
                },
                onOdczytyClicked = {
                    val encodedMiasto = URLEncoder.encode(miasto, "UTF-8")
                    val encodedNazwa = URLEncoder.encode(nazwaOgrodu, "UTF-8")
                    navController.navigate("odczyty/$ogrodId/$encodedMiasto/$encodedNazwa")
                },
                onListaLicznikowClicked = {  // ✅ DODAJ TO
                    val encodedMiasto = URLEncoder.encode(miasto, "UTF-8")
                    val encodedNazwa = URLEncoder.encode(nazwaOgrodu, "UTF-8")
                    navController.navigate("lista-licznikow/$ogrodId/$encodedMiasto/$encodedNazwa")
                }
            )
        }

        composable("odczyty/{ogrodId}/{miasto}/{nazwaOgrodu}") { backStackEntry ->
            val ogrodId = backStackEntry.arguments?.getString("ogrodId") ?: ""
            val miasto = URLDecoder.decode(backStackEntry.arguments?.getString("miasto") ?: "", "UTF-8")
            val nazwaOgrodu = URLDecoder.decode(backStackEntry.arguments?.getString("nazwaOgrodu") ?: "", "UTF-8")

            OdczytyLicznikowScreen(
                ogrodId = ogrodId,
                miasto = miasto,
                nazwaOgrodu = nazwaOgrodu,
                onBackPressed = {
                    navController.popBackStack()
                },
                onLogoutClicked = {
                    navController.navigate("ogrody") {
                        popUpTo("ogrody") { inclusive = true }
                    }
                }
            )
        }
    }
}