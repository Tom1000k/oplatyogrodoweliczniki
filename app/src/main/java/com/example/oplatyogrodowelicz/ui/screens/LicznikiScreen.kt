// LicznikiScreen.kt - z dodaną instrukcją
package com.example.oplatyogrodowelicz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.oplatyogrodowelicz.BuildConfig
import com.example.oplatyogrodowelicz.ui.components.InstrukcjaDialog
import com.example.oplatyogrodowelicz.ui.components.InstrukcjaEkran

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicznikiScreen(
    ogrodId: String,
    miasto: String,
    nazwaOgrodu: String,
    onBackPressed: () -> Unit,
    onLogoutClicked: () -> Unit,
    onOdczytyClicked: () -> Unit,
    onListaLicznikowClicked: () -> Unit
) {
    var showInstrukcja by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Liczniki",
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
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Powitanie
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Zarządzanie licznikami",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "$nazwaOgrodu, $miasto",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            // Przycisk Odczyty liczników
            LicznikiButton(
                title = "Odczyty liczników",
                description = "Wprowadzaj i przeglądaj odczyty",
                icon = Icons.Default.Visibility,
                onClick = onOdczytyClicked
            )

            // Przycisk Instrukcja
            Spacer(modifier = Modifier.height(16.dp))
            LicznikiButton(
                title = "Instrukcja",
                description = "Jak wprowadzać odczyty",
                icon = Icons.Default.Help,
                onClick = { showInstrukcja = true }
            )
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

    // Dialog z instrukcją
    if (showInstrukcja) {
        InstrukcjaDialog(
            ekran = InstrukcjaEkran.LICZNIKI,
            onDismiss = { showInstrukcja = false }
        )
    }
}

@Composable
private fun LicznikiButton(
    title: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxSize(),
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.onSurface,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    modifier = Modifier
                        .size(40.dp)
                        .padding(end = 16.dp),
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )

                Column(
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = description,
                        fontSize = 14.sp,
                        color = if (enabled) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
}
