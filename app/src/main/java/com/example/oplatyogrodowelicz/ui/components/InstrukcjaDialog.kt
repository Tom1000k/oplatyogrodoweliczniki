// InstrukcjaDialog.kt
// Lokalizacja: app/src/main/java/com/example/oplatyogrodowegosp/ui/components/InstrukcjaDialog.kt
package com.example.oplatyogrodowelicz.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

enum class InstrukcjaEkran {
    OGRODY,      // Pełna instrukcja
    LOGIN,       // Bez wyboru miasta/ogrodu
    LICZNIKI,    // Bez logowania
    ODCZYTY      // Tylko wprowadzanie odczytów
}

@Composable
fun InstrukcjaDialog(
    ekran: InstrukcjaEkran = InstrukcjaEkran.OGRODY,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Instrukcja dla Gospodarza",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )

                // Sekcja 1: Logowanie (tylko dla OGRODY)
                if (ekran == InstrukcjaEkran.OGRODY) {
                    InstrukcjaSekcja(
                        tytul = "1. Wybór ogrodu",
                        kroki = listOf(
                            "Wybierz miasto z listy",
                            "Wybierz swój ogród",
                            "Kliknij \"Kontynuuj\""
                        )
                    )
                }

                // Sekcja 2: Logowanie (dla OGRODY i LOGIN)
                if (ekran == InstrukcjaEkran.OGRODY || ekran == InstrukcjaEkran.LOGIN) {
                    val numerSekcji = if (ekran == InstrukcjaEkran.OGRODY) "2" else "1"
                    InstrukcjaSekcja(
                        tytul = "$numerSekcji. Logowanie",
                        kroki = listOf(
                            "Wybierz swoje nazwisko z listy",
                            "Wpisz hasło otrzymane od administratora",
                            "Zaznacz \"Zapamiętaj mnie\" (opcjonalnie)",
                            "Kliknij \"Zaloguj się\""
                        )
                    )
                }

                // Sekcja 3: Nawigacja (dla OGRODY, LOGIN, LICZNIKI)
                if (ekran != InstrukcjaEkran.ODCZYTY) {
                    val numerSekcji = when (ekran) {
                        InstrukcjaEkran.OGRODY -> "3"
                        InstrukcjaEkran.LOGIN -> "2"
                        InstrukcjaEkran.LICZNIKI -> "1"
                        else -> "1"
                    }
                    InstrukcjaSekcja(
                        tytul = "$numerSekcji. Przejście do odczytów",
                        kroki = listOf(
                            "Kliknij \"Odczyty liczników\""
                        )
                    )
                }

                // Sekcja 4: Wprowadzanie odczytów (zawsze)
                val numerSekcji = when (ekran) {
                    InstrukcjaEkran.OGRODY -> "4"
                    InstrukcjaEkran.LOGIN -> "3"
                    InstrukcjaEkran.LICZNIKI -> "2"
                    InstrukcjaEkran.ODCZYTY -> "1"
                }
                InstrukcjaSekcja(
                    tytul = "$numerSekcji. Wprowadzanie odczytów",
                    kroki = listOf(
                        "Wybierz typ: Prąd lub Woda",
                        if (ekran == InstrukcjaEkran.ODCZYTY) "Wybierz Teren lub Sektor (jeśli dostępne)" else "Wybierz Teren lub Sektor",
                        "Kliknij \"Wyświetl\"",
                        "Wpisz aktualny stan licznika",
                        "Kliknij przycisk \"►\" aby zapisać"
                    )
                )

                // Sekcja 5: Status odczytów (zawsze)
                val numerStatusu = when (ekran) {
                    InstrukcjaEkran.OGRODY -> "5"
                    InstrukcjaEkran.LOGIN -> "4"
                    InstrukcjaEkran.LICZNIKI -> "3"
                    InstrukcjaEkran.ODCZYTY -> "2"
                }
                InstrukcjaSekcja(
                    tytul = "$numerStatusu. Status odczytów",
                    kroki = listOf(
                        "\"►\" - można wprowadzić nowy odczyt",
                        "\"✓\" - odczyt został zapisany",
                        "\"🔒\" - odczyt poprawiony (dalsze zmiany zablokowane)"
                    )
                )

                Text(
                    text = "💡 Każdy odczyt można poprawić tylko raz!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 12.dp),
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Zamknij", fontSize = 16.sp)
                }
            }
        }
    }
}

@Composable
private fun InstrukcjaSekcja(
    tytul: String,
    kroki: List<String>
) {
    Text(
        text = tytul,
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 8.dp, top = 12.dp)
    )

    kroki.forEach { krok ->
        Text(
            text = "• $krok",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp, start = 8.dp)
        )
    }
}