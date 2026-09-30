// LoginScreen.kt
// Lokalizacja: app/src/main/java/com/example/smsapp/ui/screens/LoginScreen.kt
package com.example.oplatyogrodowelicz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.oplatyogrodowelicz.data.SharedPreferencesManager
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.oplatyogrodowelicz.BuildConfig
import com.example.oplatyogrodowelicz.ui.components.InstrukcjaDialog
import com.example.oplatyogrodowelicz.ui.components.InstrukcjaEkran
import com.example.oplatyogrodowelicz.ui.common.HandleUiMessage
import androidx.compose.runtime.collectAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    ogrodId: String,
    miasto: String,
    nazwaOgrodu: String,
    onLoginSuccess: () -> Unit,
    onBackPressed: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val context = LocalContext.current
    val rememberedOgrodId by remember { mutableStateOf(ogrodId) }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var usernameExpanded by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val prefsManager = remember { SharedPreferencesManager(context) }
    var showInstrukcja by remember { mutableStateOf(false) }

    // ← ZMIANA: Nowy state
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // ← ZMIANA: Nowa obsługa komunikatów
    HandleUiMessage(
        message = uiState.message,
        snackbarHostState = snackbarHostState,
        onMessageShown = { viewModel.clearMessage() }
    )

    // Załaduj zapisane dane przy starcie
    LaunchedEffect(Unit) {
        val (savedOgrodId, savedUsername, savedRememberMe) = prefsManager.getSavedLoginData()

        if (savedRememberMe && savedOgrodId == rememberedOgrodId) {
            username = savedUsername ?: ""
            rememberMe = savedRememberMe

        } else {

        }
    }

    // Załaduj użytkowników przy starcie
    LaunchedEffect(rememberedOgrodId) {
        viewModel.loadUsers(rememberedOgrodId)
    }

    LaunchedEffect(username) {
        if (rememberedOgrodId.equals("DEMO", ignoreCase = true) &&
            username.equals("Demo", ignoreCase = true)) {

            password = "Demo"

            // Pokaż toast
            android.widget.Toast.makeText(
                context,
                "Ogród Demo, automatyczne logowanie",
                android.widget.Toast.LENGTH_LONG
            ).show()

            // Czekaj sekundę i automatycznie zaloguj
            kotlinx.coroutines.delay(500)

            // Automatyczne logowanie
            viewModel.login(
                ogrodId = rememberedOgrodId,
                username = username,
                password = password,
                rememberMe = rememberMe,
                miasto = miasto,
                nazwaOgrodu = nazwaOgrodu,
                prefsManager = prefsManager,
                onSuccess = onLoginSuccess
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Logowanie",
                        fontSize = 24.sp,
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
        snackbarHost = { SnackbarHost(snackbarHostState) }  // ← ZMIANA: Dodany SnackbarHost
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Tytuł
            Text(
                text = "Zaloguj się do systemu",
                fontSize = 20.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 8.dp)
            )


            Text(
                text = "Ogród: $rememberedOgrodId",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Dropdown użytkowników
            if (uiState.isLoadingUsers) {
                OutlinedTextField(
                    value = "Ładowanie użytkowników...",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            } else if (uiState.hasError) {  // ← DODAJ TO
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                    OutlinedTextField(
                        value = "Błąd ładowania użytkowników",
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Button(
                        onClick = { viewModel.loadUsers(rememberedOgrodId) },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Text("Spróbuj ponownie")
                    }
                }
            } else {
                ExposedDropdownMenuBox(
                    expanded = usernameExpanded,
                    onExpandedChange = { usernameExpanded = !usernameExpanded },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    OutlinedTextField(
                        value = username,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Wybierz użytkownika") },
                        placeholder = { Text("Kliknij aby wybrać") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = usernameExpanded)
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = usernameExpanded,
                        onDismissRequest = { usernameExpanded = false }
                    ) {
                        viewModel.users.forEach { user ->
                            DropdownMenuItem(
                                text = { Text(user) },
                                onClick = {
                                    username = user
                                    usernameExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            // Pole hasła
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Hasło") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = if (passwordVisible) "Ukryj hasło" else "Pokaż hasło"
                        )
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Go
                ),
                keyboardActions = KeyboardActions(
                    onGo = {
                        keyboardController?.hide()
                        viewModel.login(
                            ogrodId = rememberedOgrodId,
                            username = username,
                            password = password,
                            rememberMe = rememberMe,
                            miasto = miasto,
                            nazwaOgrodu = nazwaOgrodu,
                            prefsManager = prefsManager,
                            onSuccess = onLoginSuccess
                        )
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Checkbox "Zapamiętaj mnie"
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Zapamiętaj mnie",
                    fontSize = 14.sp
                )
            }

            // Przycisk logowania
            Button(
                onClick = {
                    keyboardController?.hide()
                    viewModel.login(
                        ogrodId = rememberedOgrodId,
                        username = username,
                        password = password,
                        rememberMe = rememberMe,
                        miasto = miasto,
                        nazwaOgrodu = nazwaOgrodu,
                        prefsManager = prefsManager,
                        onSuccess = onLoginSuccess
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = username.isNotBlank() && password.isNotBlank() && !viewModel.isLogging,
                shape = RoundedCornerShape(12.dp)
            ) {
                if (viewModel.isLogging) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Text(
                        text = "Zaloguj się",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Informacja pomocnicza
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
                        text = "Potrzebujesz pomocy?",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Skontaktuj się z administratorem oplatyogrodowe.pl, aby uzyskać dane logowania.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
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
    if (showInstrukcja) {
        InstrukcjaDialog(
            ekran = InstrukcjaEkran.LOGIN,
            onDismiss = { showInstrukcja = false }
        )
    }
}