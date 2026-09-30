// AppConfig.kt
// Lokalizacja: app/src/main/java/com/example/smsapp/AppConfig.kt
package com.example.oplatyogrodowelicz

object AppConfig {
    // ========================================
    // KONFIGURACJA APLIKACJI - ZMIEŃ TUTAJ
    // ========================================

    /**
     * ID ogrodu do obsługi
     * ZMIEŃ TĘ WARTOŚĆ WEDŁUG POTRZEB:
     * "1", "2", "3", "4", "5" itp.
     */
    const val OGROD_ID = "DEMO1"

    /**
     * URL serwera API
     */
    const val BASE_URL = "https://oplatyogrodowe.pl/aplikacja"

    /**
     * Timeout połączenia w milisekundach
     */
    const val CONNECTION_TIMEOUT = 20000

    /**
     * Timeout odczytu w milisekundach
     */
    const val READ_TIMEOUT = 20000

}