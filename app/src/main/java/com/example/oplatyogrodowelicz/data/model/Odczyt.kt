// Odczyt.kt
// Lokalizacja: app/src/main/java/com/example/oplatyogrodoweadm/data/model/Odczyt.kt
package com.example.oplatyogrodowelicz.data.model

data class Odczyt(
    val iddzialki: String,
    val numerdzialki: String,
    val idlicznika: String,
    val numerlicznika: String,
    val ostatniOdczyt: String,
    val dataOstOdczytu: String,
    val dzialkowiec: String
)

data class OdczytyData(
    val odczyty: List<Odczyt>,
    val tereny: List<Teren> = emptyList(),
    val sektory: List<Sektor> = emptyList()
)