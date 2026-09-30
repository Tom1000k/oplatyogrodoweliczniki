// Dzialka.kt
// Lokalizacja: app/src/main/java/com/example/oplatyogrodoweadm/data/model/Dzialka.kt
package com.example.oplatyogrodowelicz.data.model

data class Dzialka(
    val iddzialki: String,
    val numerdzialki: String,
    val idtereny: String = "",
    val typHasla: String = "0",
    val metraz: String = "",           // powierzchnia z @PZD_DZIALKI
    val numerPin: String = "",         // pin z $querypiny
    val nazwaDzial: String = "",
    val malzonek: String = "",       // NAZWAKONTR
    val telefonDzial: String = "",     // TELEFON
    val emailDzial: String = "",       // EMAIL
    val kodMiastaDzial: String = "",   // KODMIASTAKONTR
    val miastoDzial: String = "",      // MIASTOKONTR
    val adresDzial: String = "",
    val idsektory: String = ""       // ADRESKONTR
)

data class Teren(
    val id: String,
    val nazwa: String
)

data class Sektor(
    val idsektory: String,
    val nazwasektora: String
)

data class TerenZDzialkami(
    val teren: Teren,
    val dzialki: List<Dzialka>
)

sealed class DzialkiData {
    data class Single(val dzialki: List<Dzialka>, val sektory: List<Sektor> = emptyList()) : DzialkiData()
    data class Multi(val terenyZDzialkami: List<TerenZDzialkami>, val sektory: List<Sektor> = emptyList()) : DzialkiData()
}