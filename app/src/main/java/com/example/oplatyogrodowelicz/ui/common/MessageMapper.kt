// MessageMapper.kt
// Lokalizacja: app/src/main/java/com/example/oplatyogrodowelicz/ui/common/MessageMapper.kt
package com.example.oplatyogrodowelicz.ui.common

object MessageMapper {

    fun mapErrorToUserFriendly(errorMessage: String): UiMessage.Error {
        return when {
            // Błędy braku internetu - DODAJ TO NA POCZĄTKU
            errorMessage.contains("network is unreachable", ignoreCase = true) ||
                    errorMessage.contains("no network", ignoreCase = true) ||
                    errorMessage.contains("internet connection", ignoreCase = true) ||
                    errorMessage.contains("host is unresolved", ignoreCase = true) ||
                    errorMessage.contains("unable to resolve host", ignoreCase = true) ->
                UiMessage.Error("Brak połączenia z internetem", "Spróbuj ponownie")

            // Błędy sieciowe - ROZSZERZ TO
            errorMessage.contains("timeout", ignoreCase = true) ||
                    errorMessage.contains("sprawdź połączenie", ignoreCase = true) ||
                    errorMessage.contains("connectexception", ignoreCase = true) ||
                    errorMessage.contains("socketexception", ignoreCase = true) ||
                    errorMessage.contains("unknownhostexception", ignoreCase = true) ->
                UiMessage.Error("Sprawdź połączenie z internetem", "Spróbuj ponownie")

            errorMessage.contains("nie można połączyć", ignoreCase = true) ||
                    errorMessage.contains("connectexception", ignoreCase = true) ->
                UiMessage.Error("Serwer niedostępny. Spróbuj później", "OK")

            // Błędy autoryzacji
            errorMessage.contains("błąd autoryzacji", ignoreCase = true) ||
                    errorMessage.contains("unauthorized", ignoreCase = true) ->
                UiMessage.Error("Problem z logowaniem. Skontaktuj się z administratorem", "OK")

            errorMessage.contains("nieprawidłowe dane", ignoreCase = true) ->
                UiMessage.Error("Nieprawidłowe hasło lub użytkownik", "Spróbuj ponownie")

            errorMessage.contains("za dużo prób logowania", ignoreCase = true) ||
                    errorMessage.contains("spróbuj za 5 minut", ignoreCase = true) ->
                UiMessage.Error("Za dużo nieudanych prób. Odczekaj 5 minut przed kolejną próbą")

            // Błędy danych
            errorMessage.contains("błąd parsowania", ignoreCase = true) ->
                UiMessage.Error("Problem z danymi. Spróbuj ponownie", "OK")

            errorMessage.contains("brak odczytów", ignoreCase = true) ->
                UiMessage.Error("Brak liczników dla wybranych kryteriów")

            // DEMO
            errorMessage.contains("demo nie dokonujemy zapisów", ignoreCase = true) ->
                UiMessage.Error("To ogród demonstracyjny - zapis zablokowany")

            errorMessage.contains("demo - zapis zablokowany", ignoreCase = true) ->
                UiMessage.Error("To ogród demonstracyjny - zapis zablokowany")

            errorMessage.contains("już został wprowadzony", ignoreCase = true) ->
                UiMessage.Error("Odczyt już wprowadzony dzisiaj")

            // Błędy ładowania
            errorMessage.contains("błąd pobierania", ignoreCase = true) ->
                UiMessage.Error("Nie udało się pobrać danych", "Odśwież")

            errorMessage.contains("błąd ładowania", ignoreCase = true) ->
                UiMessage.Error("Problem z ładowaniem", "Spróbuj ponownie")

            errorMessage.contains("odczyty wylaczone", ignoreCase = true) ->
                UiMessage.Error("Wprowadzanie nieaktywne", "OK")

            // Domyślny błąd
            else -> UiMessage.Error("Wystąpił nieoczekiwany problem", "OK")
        }
    }

    fun mapSuccessMessage(message: String): UiMessage.Success {
        return when {
            message.contains("zapisany", ignoreCase = true) ->
                UiMessage.Success("✓ Odczyt zapisany")

            message.contains("zaktualizowany", ignoreCase = true) ->
                UiMessage.Success("✓ Odczyt zaktualizowany")

            else -> UiMessage.Success(message)
        }
    }

    fun createInfoMessage(message: String): UiMessage.Info {
        return UiMessage.Info(message)
    }
}