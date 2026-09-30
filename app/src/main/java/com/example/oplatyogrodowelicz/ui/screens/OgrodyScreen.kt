// OgrodyScreen.kt
// Lokalizacja: app/src/main/java/com/example/smsapp/ui/screens/OgrodyScreen.kt
package com.example.oplatyogrodowelicz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oplatyogrodowelicz.data.model.Ogrod
import android.content.Intent
import android.net.Uri
import androidx.activity.ComponentActivity
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Help
import androidx.compose.ui.platform.LocalContext
import com.example.oplatyogrodowelicz.BuildConfig
import com.example.oplatyogrodowelicz.ui.components.InstrukcjaDialog
import com.example.oplatyogrodowelicz.ui.components.InstrukcjaEkran
import com.example.oplatyogrodowelicz.ui.common.HandleUiMessage
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import com.example.oplatyogrodowelicz.ui.common.UiMessage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OgrodyScreen(
    onOgrodSelected: (Ogrod, String) -> Unit,
    viewModel: OgrodyViewModel = viewModel()
) {
    // ← ZMIANA: Nowy state
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // ← ZMIANA: Nowa obsługa komunikatów
    HandleUiMessage(
        message = uiState.message,
        snackbarHostState = snackbarHostState,
        onMessageShown = { viewModel.clearMessage() }
    )
    val coroutineScope = rememberCoroutineScope()

    // Stany dla UI
    var miastoExpanded by remember { mutableStateOf(false) }
    var ogrodExpanded by remember { mutableStateOf(false) }
    var selectedMiasto by remember { mutableStateOf<String?>(null) }
    var selectedOgrod by remember { mutableStateOf<Ogrod?>(null) }
    var showInstrukcja by remember { mutableStateOf(false) }

    // Załaduj miasta przy starcie
    LaunchedEffect(Unit) {
        viewModel.loadMiasta()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Opłaty Ogrodowe Liczniki",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = { showInstrukcja = true }) {
                        Icon(
                            imageVector = Icons.Default.Help,
                            contentDescription = "Instrukcja",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            when {
                // ← ZMIANA: Używamy uiState zamiast viewModel properties
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Ładowanie ogrodów...")
                        }
                    }
                }

                // ← ZMIANA: Sprawdzamy czy jest błąd, ale nie wyświetlamy go tutaj (idzie przez Snackbar)
                uiState.hasError -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Nie udało się załadować danych.\nSprawdź połączenie z internetem.",
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadMiasta() }
                            ) {
                                Text("Spróbuj ponownie")
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            val context = LocalContext.current
                            OutlinedButton(
                                onClick = {
                                    (context as? ComponentActivity)?.finishAffinity()
                                }
                            ) {
                                Text("Zamknij aplikację")
                            }
                        }
                    }
                }

                else -> {
                    // Tekst powitalny
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(bottom = 24.dp)
                    ) {
                        Text(
                            text = "Witaj w aplikacji Opłaty Ogrodowe Liczniki.",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Text(
                            text = "Wybierz swój ogród, aby kontynuować",
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Krok 1: Wybór miasta
                    Text(
                        text = "Wybierz miasto",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    ExposedDropdownMenuBox(
                        expanded = miastoExpanded,
                        onExpandedChange = { miastoExpanded = !miastoExpanded },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = selectedMiasto ?: "",
                            onValueChange = {},
                            readOnly = true,
                            placeholder = { Text("Wybierz miasto") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = miastoExpanded)
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )

                        ExposedDropdownMenu(
                            expanded = miastoExpanded,
                            onDismissRequest = { miastoExpanded = false }
                        ) {
                            uiState.miastaList.forEach { miastoZOgrodami ->
                            DropdownMenuItem(
                                    text = { Text(miastoZOgrodami.miasto) },
                                    onClick = {
                                        selectedMiasto = miastoZOgrodami.miasto
                                        selectedOgrod = null // Reset wyboru ogrodu
                                        miastoExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Krok 2: Wybór ogrodu (tylko jeśli wybrano miasto)
                    if (selectedMiasto != null) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Wybierz ogród",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        val ogrodyWMiescie = uiState.miastaList
                            .find { it.miasto == selectedMiasto }?.ogrody ?: emptyList()

                        ExposedDropdownMenuBox(
                            expanded = ogrodExpanded,
                            onExpandedChange = { ogrodExpanded = !ogrodExpanded },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = selectedOgrod?.nazwa ?: "",
                                onValueChange = {},
                                readOnly = true,
                                placeholder = { Text("Wybierz ogród") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = ogrodExpanded)
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = ogrodExpanded,
                                onDismissRequest = { ogrodExpanded = false }
                            ) {
                                ogrodyWMiescie.forEach { ogrod ->
                                    DropdownMenuItem(
                                        text = { Text(ogrod.nazwa) },
                                        onClick = {
                                            selectedOgrod = ogrod
                                            ogrodExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Przycisk kontynuuj
                    if (selectedOgrod != null && selectedMiasto != null) {
                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                onOgrodSelected(selectedOgrod!!, selectedMiasto!!)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Kontynuuj",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Karta z wybranym ogrodem
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text(
                                    text = "Wybrany ogród:",
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = "${selectedOgrod!!.nazwa}, $selectedMiasto",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    // Informacje dodatkowe
                    Spacer(modifier = Modifier.height(32.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            var kontaktExpanded by remember { mutableStateOf(false) }

                            Text(
                                text = "Jeśli nie widzisz swojego ogrodu na liście, reprezentujesz zarząd ROD i jesteś zainteresowany rozszerzeniem możliwości zarządzania ogrodem:",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            Button(
                                onClick = {
                                    kontaktExpanded = !kontaktExpanded
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Dowiedz się więcej",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Opcje kontaktu (pokazują się po kliknięciu)
                            if (kontaktExpanded) {
                                val context = LocalContext.current
                                val emailSubject = "Zapytanie o system ROD"

                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = {
                                        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                            data = Uri.parse("mailto:oplatyogrodowe@gmail.com")
                                            putExtra(Intent.EXTRA_SUBJECT, emailSubject)
                                        }
                                        context.startActivity(emailIntent)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Napisz email")
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        val phoneIntent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:+48600597468")
                                        }
                                        context.startActivity(phoneIntent)
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Zadzwoń")
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    // Informacje o aplikacji
                    Text(
                        text = "v ${BuildConfig.VERSION_NAME}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
    if (showInstrukcja) {
        InstrukcjaDialog(
            ekran = InstrukcjaEkran.OGRODY,
            onDismiss = { showInstrukcja = false }
        )
    }
}