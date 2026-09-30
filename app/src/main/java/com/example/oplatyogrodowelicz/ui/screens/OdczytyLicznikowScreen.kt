// OdczytyLicznikowScreen.kt
// Lokalizacja: app/src/main/java/com/example/oplatyogrodoweadm/ui/screens/OdczytyLicznikowScreen.kt
package com.example.oplatyogrodowelicz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oplatyogrodowelicz.data.model.Odczyt
import com.example.oplatyogrodowelicz.data.model.Teren
import com.example.oplatyogrodowelicz.data.model.Sektor
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.Help
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import com.example.oplatyogrodowelicz.ui.components.InstrukcjaDialog
import com.example.oplatyogrodowelicz.ui.components.InstrukcjaEkran
import com.example.oplatyogrodowelicz.ui.common.HandleUiMessage


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OdczytyLicznikowScreen(
    ogrodId: String,
    miasto: String,
    nazwaOgrodu: String,
    onBackPressed: () -> Unit,
    onLogoutClicked: () -> Unit,
    viewModel: OdczytyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showInstrukcja by remember { mutableStateOf(false) }

    // Załaduj dane filtrów przy pierwszym wyświetleniu
    LaunchedEffect(ogrodId) {
        viewModel.loadFiltersData(ogrodId)
    }

    // ← ZMIANA: Nowa obsługa komunikatów
    HandleUiMessage(
        message = uiState.message,
        snackbarHostState = snackbarHostState,
        onMessageShown = { viewModel.clearMessage() }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Odczyty liczników",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Wstecz",
                            tint = Color.White
                        )
                    }
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
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }  // DODAJ TO
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp)
                ) {
                    Text(
                        text = "$nazwaOgrodu, $miasto",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Filters Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Filtry",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    // Radio Buttons - Typ licznika
                    Text(
                        text = "Typ licznika:",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {

                        Row(
                            modifier = Modifier
                                .selectable(
                                    selected = uiState.selectedTypLicznika == "prad",
                                    onClick = { viewModel.setTypLicznika("prad") },
                                    role = Role.RadioButton
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.selectedTypLicznika == "prad",
                                onClick = null
                            )
                            Text(
                                text = "Prąd",
                                modifier = Modifier.padding(start = 8.dp),
                                fontSize = 14.sp
                            )
                        }
                        Row(
                            modifier = Modifier
                                .selectable(
                                    selected = uiState.selectedTypLicznika == "woda",
                                    onClick = { viewModel.setTypLicznika("woda") },
                                    role = Role.RadioButton
                                )
                                .padding(end = 24.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = uiState.selectedTypLicznika == "woda",
                                onClick = null
                            )
                            Text(
                                text = "Woda",
                                modifier = Modifier.padding(start = 8.dp),
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Dropdowny - logika wyświetlania
                    val hasTerenow = uiState.tereny.size > 1
                    val hasSektorow = uiState.sektory.size > 1

                    if (hasTerenow && hasSektorow) {
                        // Dwa dropdown obok siebie
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Dropdown Terenów
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Teren:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(bottom = 5.dp)
                                )
                                TerenDropdown(
                                    tereny = uiState.tereny,
                                    selectedTerenId = uiState.selectedTerenId,
                                    onTerenSelected = { viewModel.setSelectedTeren(it) }
                                )
                            }

                            // Dropdown Sektorów
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sektor:",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(bottom = 5.dp)
                                )
                                SektorDropdown(
                                    sektory = uiState.sektory,
                                    selectedSektorId = uiState.selectedSektorId,
                                    onSektorSelected = { viewModel.setSelectedSektor(it) }
                                )
                            }
                        }
                    } else if (hasTerenow) {
                        // Tylko dropdown terenów
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Text(
                                text = "Teren:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            TerenDropdown(
                                tereny = uiState.tereny,
                                selectedTerenId = uiState.selectedTerenId,
                                onTerenSelected = { viewModel.setSelectedTeren(it) }
                            )
                        }
                    } else if (hasSektorow) {
                        // Tylko dropdown sektorów
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Text(
                                text = "Sektor:",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            SektorDropdown(
                                sektory = uiState.sektory,
                                selectedSektorId = uiState.selectedSektorId,
                                onSektorSelected = { viewModel.setSelectedSektor(it) }
                            )
                        }
                    }

                    // Przycisk Wyświetl
                    Button(
                        onClick = { viewModel.wyswietlOdczyty(ogrodId) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        //enabled = !uiState.isLoading,
                        enabled = !uiState.isLoading && (!(hasTerenow || hasSektorow) || (uiState.selectedTerenId != null || uiState.selectedSektorId != null)),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Wyświetl",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Wyniki - tabela odczytów z nagłówkami
            if (uiState.showOdczyty) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp)
                    ) {
                        Text(
                            text = "Wyniki (${uiState.odczyty.size})",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        if (uiState.odczyty.isEmpty()) {
                            Text(
                                text = "Brak odczytów dla wybranych kryteriów",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        } else {
                            // Stałe nagłówki tabeli
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 5.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Działka\nLicznik",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Stan\nData",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(0.8f),
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "Nowy odczyt",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1.2f),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }

                            Divider(
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )

                            // LazyColumn z danymi
                            LazyColumn(
                                modifier = Modifier.height(600.dp),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                items(
                                    items = uiState.odczyty,
                                    key = { odczyt -> "${odczyt.idlicznika}_${uiState.odczyty.indexOf(odczyt)}" }
                                ) { odczyt ->
                                    OdczytTableRow(
                                        odczyt = odczyt,
                                        onNowyOdczytChange = { newValue ->
                                            // Na razie puste - wartość przechowuje sam komponent
                                        },
                                        onWstawClicked = { nowyOdczyt ->
                                            viewModel.zapiszOdczyt(ogrodId, odczyt.idlicznika, nowyOdczyt)
                                        },
                                        isZapisany = viewModel.isLicznikZapisany(odczyt.idlicznika) ,
                                        isZaktualizowany = viewModel.isLicznikZaktualizowany(odczyt.idlicznika)  // DODAJ TO
                                    )

                                    if (odczyt != uiState.odczyty.last()) {
                                        Divider(
                                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                            thickness = 0.5.dp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Error message
         /*   uiState.errorMessage?.let { error ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        fontSize = 14.sp
                    )
                }
            }*/
        }
    }
    if (showInstrukcja) {
        InstrukcjaDialog(
            ekran = InstrukcjaEkran.ODCZYTY,
            onDismiss = { showInstrukcja = false }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TerenDropdown(
    tereny: List<Teren>,
    selectedTerenId: String?,
    onTerenSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedTeren = tereny.find { it.id == selectedTerenId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedTeren?.nazwa ?: "Wybierz teren",
            onValueChange = { },
            readOnly = true,
            label = { Text("Wybierz teren") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            /*
            DropdownMenuItem(
                text = { Text("Wszystkie") },
                onClick = {
                    onTerenSelected(null)
                    expanded = false
                }
            )
            */
            tereny.forEach { teren ->
                DropdownMenuItem(
                    text = { Text(teren.nazwa) },
                    onClick = {
                        onTerenSelected(teren.id)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SektorDropdown(
    sektory: List<Sektor>,
    selectedSektorId: String?,
    onSektorSelected: (String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedSektor = sektory.find { it.idsektory == selectedSektorId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedSektor?.nazwasektora ?: "Wybierz sektor",
            onValueChange = { },
            readOnly = true,
            label = { Text("Wybierz sektor") },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = "Dropdown"
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            shape = RoundedCornerShape(12.dp)
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            /*
            DropdownMenuItem(
                text = { Text("Wszystkie") },
                onClick = {
                    onSektorSelected(null)
                    expanded = false
                }
            )
            */
            sektory.forEach { sektor ->
                DropdownMenuItem(
                    text = { Text(sektor.nazwasektora) },
                    onClick = {
                        onSektorSelected(sektor.idsektory)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun OdczytTableRow(
    odczyt: Odczyt,
    onNowyOdczytChange: (String) -> Unit,
    onWstawClicked: (String) -> Unit,
    isZapisany: Boolean,
    isZaktualizowany: Boolean
) {
    var nowyOdczytValue by remember(odczyt.idlicznika) { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Działka + Licznik
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = odczyt.numerdzialki,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = odczyt.numerlicznika,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Stan + Data
        Column(modifier = Modifier.weight(0.8f)) {
            Text(
                text = odczyt.ostatniOdczyt,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = odczyt.dataOstOdczytu,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Input + Przycisk
        Row(
            modifier = Modifier.weight(1.2f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = nowyOdczytValue,
                onValueChange = {
                    nowyOdczytValue = it
                    onNowyOdczytChange(it)
                },
                modifier = Modifier.weight(1f),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 12.sp),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                )
            )

            Button(
                onClick = {
                    keyboardController?.hide()
                    onWstawClicked(nowyOdczytValue) },
                modifier = Modifier.size(40.dp),
                contentPadding = PaddingValues(0.dp),
                enabled = nowyOdczytValue.isNotBlank() && !isZaktualizowany,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when {
                        isZaktualizowany -> "🔒"  // Zablokowany
                        isZapisany -> "✓"        // Zapisany
                        else -> "►"              // Można wprowadzić
                    },
                    fontSize = 16.sp
                )
            }
        }
    }
}