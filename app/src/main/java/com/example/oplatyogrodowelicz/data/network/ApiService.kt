// ApiService.kt
// Lokalizacja: app/src/main/java/com/example/smsapp/data/network/ApiService.kt
package com.example.oplatyogrodowelicz.data.network

import com.example.oplatyogrodowelicz.AppConfig
import com.example.oplatyogrodowelicz.data.model.MiastoZOgrodami
import com.example.oplatyogrodowelicz.data.model.Ogrod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import com.example.oplatyogrodowelicz.data.model.*
import com.example.oplatyogrodowelicz.BuildConfig

class ApiService {
    private val baseUrl = AppConfig.BASE_URL
    private val apiKey = BuildConfig.API_KEY


    // Pobieranie listy miast z ogrodami
    suspend fun pobierzMiastaZOgrodami(): List<MiastoZOgrodami> = withContext(Dispatchers.IO) {
        val url = URL("$baseUrl/get_gardens.php")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("User-Agent", "OplatyOgrodoweApp/1.0")
            connection.setRequestProperty("X-API-KEY", apiKey)  // ← DODANE
            connection.connectTimeout = AppConfig.CONNECTION_TIMEOUT
            connection.readTimeout = AppConfig.READ_TIMEOUT

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                parseMiastaZOgrodami(response)
            } else {
                throw Exception("HTTP Error: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }

    // Logowanie użytkownika
    suspend fun login(ogrodId: String, username: String, password: String): Boolean = withContext(Dispatchers.IO) {
        val url = URL("$baseUrl/sender/login.php")
        val connection = url.openConnection() as HttpURLConnection

        try {
            val postData = "ogrod_id=${URLEncoder.encode(ogrodId, "UTF-8")}&username=${URLEncoder.encode(username, "UTF-8")}&password=${URLEncoder.encode(password, "UTF-8")}"

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.setRequestProperty("User-Agent", "OplatyOgrodoweApp/1.0")
            connection.setRequestProperty("X-API-KEY", apiKey)
            connection.setRequestProperty("Content-Length", postData.length.toString())
            connection.doOutput = true
            connection.connectTimeout = AppConfig.CONNECTION_TIMEOUT
            connection.readTimeout = AppConfig.READ_TIMEOUT

            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(postData)
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val response = reader.readText()
                reader.close()

                try {
                    // Sprawdź odpowiedź serwera
                    val jsonResponse = JSONObject(response)
                    jsonResponse.getBoolean("success")
                } catch (e: Exception) {
                    // Jeśli nie można sparsować JSON, znaczy że serwer zwrócił błąd jako tekst
                    throw Exception("Odpowiedź serwera: $response")
                }
            } else {
                // Odczytaj błąd z serwera
                val errorStream = connection.errorStream
                if (errorStream != null) {
                    val errorReader = BufferedReader(InputStreamReader(errorStream, "UTF-8"))
                    val errorResponse = errorReader.readText()
                    errorReader.close()
                    throw Exception(errorResponse)  // Surowa odpowiedź z serwera
                } else {
                    throw Exception("HTTP Error: $responseCode")
                }
            }
        } catch (e: Exception) {
            throw Exception("${e.message}")
        } finally {
            connection.disconnect()
        }
    }

    // Pobieranie listy użytkowników
    suspend fun getUsers(ogrodId: String): List<String> = withContext(Dispatchers.IO) {
        val url = URL("$baseUrl/sender/get_users.php")
        val connection = url.openConnection() as HttpURLConnection

        try {
            val postData = "ogrod_id=${URLEncoder.encode(ogrodId, "UTF-8")}"

            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.setRequestProperty("User-Agent", "OplatyOgrodoweApp/1.0")
            connection.setRequestProperty("X-API-KEY", apiKey)
            connection.setRequestProperty("Content-Length", postData.length.toString())
            connection.doOutput = true
            connection.connectTimeout = AppConfig.CONNECTION_TIMEOUT
            connection.readTimeout = AppConfig.READ_TIMEOUT

            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(postData)
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                val response = reader.readText()
                reader.close()

                parseUsers(response)
            } else {
                val errorStream = connection.errorStream
                if (errorStream != null) {
                    val errorReader = BufferedReader(InputStreamReader(errorStream, "UTF-8"))
                    val errorResponse = errorReader.readText()
                    errorReader.close()
                    throw Exception(errorResponse)
                } else {
                    throw Exception("HTTP Error: $responseCode")
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            throw Exception("Timeout - sprawdź połączenie internetowe")
        } catch (e: java.net.ConnectException) {
            throw Exception("Nie można połączyć się z serwerem")
        } catch (e: Exception) {
            throw Exception("Błąd połączenia: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }

    // Parsowanie listy użytkowników
    private fun parseUsers(jsonString: String): List<String> {
        try {
            val users = mutableListOf<String>()
            val jsonArray = JSONArray(jsonString)

            for (i in 0 until jsonArray.length()) {
                users.add(jsonArray.getString(i))
            }

            return users
        } catch (e: Exception) {
            throw Exception("Błąd parsowania użytkowników: ${e.message}")
        }
    }

    // Parsowanie miast z ogrodami
    private fun parseMiastaZOgrodami(jsonString: String): List<MiastoZOgrodami> {
        val jsonArray = JSONArray(jsonString)
        val miastaList = mutableListOf<MiastoZOgrodami>()

        for (i in 0 until jsonArray.length()) {
            val miastoJson = jsonArray.getJSONObject(i)
            val miasto = miastoJson.getString("miasto")
            val ogrodyJson = miastoJson.getJSONArray("ogrody")

            val ogrody = mutableListOf<Ogrod>()
            for (j in 0 until ogrodyJson.length()) {
                val ogrodJson = ogrodyJson.getJSONObject(j)
                ogrody.add(
                    Ogrod(
                        id = ogrodJson.getString("id"),
                        nazwa = ogrodJson.getString("nazwa")
                    )
                )
            }

            miastaList.add(MiastoZOgrodami(miasto, ogrody))
        }

        return miastaList
    }

    // Pobieranie działek
    suspend fun pobierzDzialki(ogrodId: String): com.example.oplatyogrodowelicz.data.model.DzialkiData = withContext(Dispatchers.IO) {
        val url = URL("$baseUrl/get_dzialki1.php?ogrod_id=$ogrodId")
        val connection = url.openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "GET"
            connection.setRequestProperty("Content-Type", "application/json")
            connection.setRequestProperty("User-Agent", "OplatyOgrodoweApp/1.0")
            connection.setRequestProperty("X-API-KEY", apiKey)
            connection.connectTimeout = AppConfig.CONNECTION_TIMEOUT
            connection.readTimeout = AppConfig.READ_TIMEOUT

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = reader.readText()
                reader.close()

                parseDzialkiData(response)
            } else {
                throw Exception("HTTP Error: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }

    // Parsowanie danych działek
    private fun parseDzialkiData(jsonString: String): com.example.oplatyogrodowelicz.data.model.DzialkiData {
        val jsonObject = JSONObject(jsonString)
        val typ = jsonObject.getString("typ")
        val dane = jsonObject.getJSONArray("dane")

        // DODANE: Parsowanie sektorów
        val sektory = mutableListOf<com.example.oplatyogrodowelicz.data.model.Sektor>()
        if (jsonObject.has("sektory")) {
            val sektoryArray = jsonObject.getJSONArray("sektory")
            for (i in 0 until sektoryArray.length()) {
                val sektorJson = sektoryArray.getJSONObject(i)
                sektory.add(
                    com.example.oplatyogrodowelicz.data.model.Sektor(
                        idsektory = sektorJson.getString("idsektory"),
                        nazwasektora = sektorJson.getString("nazwasektora")
                    )
                )
            }
        }

        if (typ == "single") {
            // Tylko jeden teren - lista działek
            val dzialki = mutableListOf<com.example.oplatyogrodowelicz.data.model.Dzialka>()
            for (i in 0 until dane.length()) {
                val dzialkaJson = dane.getJSONObject(i)
                dzialki.add(
                    com.example.oplatyogrodowelicz.data.model.Dzialka(
                        iddzialki = dzialkaJson.getString("iddzialki"),
                        numerdzialki = dzialkaJson.getString("numerdzialki"),
                        typHasla = dzialkaJson.optString("typ_hasla", "0"),
                        metraz = dzialkaJson.optString("metraz", ""),
                        numerPin = dzialkaJson.optString("numerPin", ""),
                        nazwaDzial = dzialkaJson.optString("nazwaDzial", ""),
                        malzonek = dzialkaJson.optString("malzonek", ""),
                        telefonDzial = dzialkaJson.optString("telefonDzial", ""),
                        emailDzial = dzialkaJson.optString("emailDzial", ""),
                        kodMiastaDzial = dzialkaJson.optString("kodMiastaDzial", ""),
                        miastoDzial = dzialkaJson.optString("miastoDzial", ""),
                        adresDzial = dzialkaJson.optString("adresDzial", ""),
                        idsektory = dzialkaJson.optString("idsektory", "")
                    )
                )
            }
            return com.example.oplatyogrodowelicz.data.model.DzialkiData.Single(dzialki, sektory)
        } else {
            // Wiele terenów - grupowane działki
            val terenyZDzialkami = mutableListOf<com.example.oplatyogrodowelicz.data.model.TerenZDzialkami>()
            for (i in 0 until dane.length()) {
                val grupedData = dane.getJSONObject(i)
                val terenJson = grupedData.getJSONObject("teren")
                val dzialkiJson = grupedData.getJSONArray("dzialki")

                val teren = com.example.oplatyogrodowelicz.data.model.Teren(
                    id = terenJson.getString("id"),
                    nazwa = terenJson.getString("nazwa")
                )

                val dzialki = mutableListOf<com.example.oplatyogrodowelicz.data.model.Dzialka>()
                for (j in 0 until dzialkiJson.length()) {
                    val dzialkaJson = dzialkiJson.getJSONObject(j)
                    dzialki.add(
                        com.example.oplatyogrodowelicz.data.model.Dzialka(
                            iddzialki = dzialkaJson.getString("iddzialki"),
                            numerdzialki = dzialkaJson.getString("numerdzialki"),
                            idtereny = dzialkaJson.getString("idtereny"),
                            typHasla = dzialkaJson.optString("typ_hasla", "0"),
                            metraz = dzialkaJson.optString("metraz", ""),
                            numerPin = dzialkaJson.optString("numerPin", ""),
                            nazwaDzial = dzialkaJson.optString("nazwaDzial", ""),
                            malzonek = dzialkaJson.optString("malzonek", ""),
                            telefonDzial = dzialkaJson.optString("telefonDzial", ""),
                            emailDzial = dzialkaJson.optString("emailDzial", ""),
                            kodMiastaDzial = dzialkaJson.optString("kodMiastaDzial", ""),
                            miastoDzial = dzialkaJson.optString("miastoDzial", ""),
                            adresDzial = dzialkaJson.optString("adresDzial", ""),
                            idsektory = dzialkaJson.optString("idsektory", "")
                        )
                    )
                }

                terenyZDzialkami.add(com.example.oplatyogrodowelicz.data.model.TerenZDzialkami(teren, dzialki))
            }
            return com.example.oplatyogrodowelicz.data.model.DzialkiData.Multi(terenyZDzialkami, sektory)
        }
    }


    suspend fun pobierzOdczyty(
        ogrodId: String,
        idsektory: String? = null,
        terenId: String? = null,
        medium: String
    ): List<Odczyt> = withContext(Dispatchers.IO) {
        val url = URL("$baseUrl/sender/get_odczyt.php")
        val connection = url.openConnection() as HttpURLConnection

        try {
            // Przygotuj dane POST
            var postData = "ogrod_id=${URLEncoder.encode(ogrodId, "UTF-8")}&medium=${URLEncoder.encode(medium, "UTF-8")}"

            if (idsektory != null) {
                postData += "&idsektory=${URLEncoder.encode(idsektory, "UTF-8")}"
            }
            if (terenId != null) {
                postData += "&terenid=${URLEncoder.encode(terenId, "UTF-8")}"
            }

            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.setRequestProperty("User-Agent", "OplatyOgrodoweApp/1.0")
            connection.setRequestProperty("X-API-KEY", apiKey)
            connection.setRequestProperty("Content-Length", postData.length.toString())
            connection.connectTimeout = AppConfig.CONNECTION_TIMEOUT
            connection.readTimeout = AppConfig.READ_TIMEOUT

            // Wyślij dane
            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(postData)
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            when (responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                    val response = reader.readText()
                    reader.close()

                    parseOdczytyData(response)
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    throw Exception("Błąd autoryzacji - sprawdź API key")
                }
                HttpURLConnection.HTTP_BAD_REQUEST -> {
                    val errorStream = connection.errorStream
                    val errorMessage = if (errorStream != null) {
                        BufferedReader(InputStreamReader(errorStream, "UTF-8")).use {
                            val errorResponse = it.readText()
                            try {
                                val errorJson = JSONObject(errorResponse)
                                errorJson.getString("error")
                            } catch (e: Exception) {
                                errorResponse
                            }
                        }
                    } else {
                        "Błędne żądanie"
                    }
                    throw Exception(errorMessage)
                }
                else -> {
                    throw Exception("HTTP Error: $responseCode")
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            throw Exception("Timeout - sprawdź połączenie internetowe")
        } catch (e: java.net.ConnectException) {
            throw Exception("Nie można połączyć się z serwerem")
        } catch (e: Exception) {
            if (e.message?.contains("HTTP Error") == true ||
                e.message?.contains("Błąd") == true ||
                e.message?.contains("Timeout") == true) {
                throw e
            }
            throw Exception("Błąd połączenia: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }

    // Parsowanie danych odczytów z JSON
    private fun parseOdczytyData(jsonString: String): List<Odczyt> {
        try {
            val jsonObject = JSONObject(jsonString)
            val odczytyArray = jsonObject.getJSONArray("odczyty")
            val odczyty = mutableListOf<Odczyt>()

            for (i in 0 until odczytyArray.length()) {
                val odczytJson = odczytyArray.getJSONObject(i)
                odczyty.add(
                    Odczyt(
                        iddzialki = odczytJson.getString("iddzialki"),
                        numerdzialki = odczytJson.getString("numerdzialki"),
                        idlicznika = odczytJson.getString("idlicznika"),
                        numerlicznika = odczytJson.getString("numerlicznika"),
                        ostatniOdczyt = odczytJson.getString("ostatniOdczyt"),
                        dataOstOdczytu = odczytJson.getString("dataOstOdczytu"),
                        dzialkowiec = odczytJson.getString("dzialkowiec")
                    )
                )
            }

            return odczyty
        } catch (e: Exception) {
            throw Exception("Błąd parsowania danych odczytów: ${e.message}")
        }
    }

    suspend fun zapiszOdczyt(
        ogrodId: String,
        idlicznika: String,
        nowyOdczyt: String
    ): Pair<Boolean, Boolean> = withContext(Dispatchers.IO) {
    val url = URL("$baseUrl/sender/zapisz_odczyt.php")
        val connection = url.openConnection() as HttpURLConnection

        try {
            // Przygotuj dane POST
            val postData = "ogrod_id=${URLEncoder.encode(ogrodId, "UTF-8")}&idlicznika=${URLEncoder.encode(idlicznika, "UTF-8")}&nowy_odczyt=${URLEncoder.encode(nowyOdczyt, "UTF-8")}"

            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.setRequestProperty("User-Agent", "OplatyOgrodoweApp/1.0")
            connection.setRequestProperty("X-API-KEY", apiKey)
            connection.setRequestProperty("Content-Length", postData.length.toString())
            connection.connectTimeout = AppConfig.CONNECTION_TIMEOUT
            connection.readTimeout = AppConfig.READ_TIMEOUT

            // Wyślij dane
            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(postData)
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            when (responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                    val response = reader.readText()
                    reader.close()

                    // Sprawdź czy sukces
                    try {
                        val jsonResponse = JSONObject(response)
                        val success = jsonResponse.getBoolean("success")
                        val wasUpdate = jsonResponse.optBoolean("updated", false)  // DODAJ TO
                        Pair(success, wasUpdate)
                    } catch (e: Exception) {
                        throw Exception("Nieprawidłowa odpowiedź serwera")
                    }
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> {  // ← DODANO obsługę 401
                    val errorStream = connection.errorStream
                    val errorMessage = if (errorStream != null) {
                        BufferedReader(InputStreamReader(errorStream, "UTF-8")).use {
                            val errorResponse = it.readText()
                            try {
                                val errorJson = JSONObject(errorResponse)
                                errorJson.getString("error")  // Wyciągnij "już został wprowadzony"
                            } catch (e: Exception) {
                                errorResponse
                            }
                        }
                    } else {
                        "Błąd autoryzacji"
                    }
                    throw Exception(errorMessage)
                }
                HttpURLConnection.HTTP_CONFLICT -> {
                    throw Exception("Odczyt już został wprowadzony dzisiaj")
                }
                HttpURLConnection.HTTP_BAD_REQUEST -> {
                    val errorStream = connection.errorStream
                    val errorMessage = if (errorStream != null) {
                        BufferedReader(InputStreamReader(errorStream, "UTF-8")).use {
                            val errorResponse = it.readText()
                            try {
                                val errorJson = JSONObject(errorResponse)
                                errorJson.getString("error")
                            } catch (e: Exception) {
                                errorResponse
                            }
                        }
                    } else {
                        "Błędne żądanie"
                    }
                    throw Exception(errorMessage)
                }
                else -> {
                    throw Exception("HTTP Error: $responseCode")
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            throw Exception("Timeout - sprawdź połączenie internetowe")
        } catch (e: java.net.ConnectException) {
            throw Exception("Nie można połączyć się z serwerem")
        } catch (e: Exception) {
            if (e.message?.contains("HTTP Error") == true ||
                e.message?.contains("Błąd") == true ||
                e.message?.contains("Timeout") == true ||
                e.message?.contains("już został wprowadzony") == true) {
                throw e
            }
            throw Exception("Błąd połączenia: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }

    suspend fun sprawdzWersje(
        appName: String,
        currentVersion: String
    ): VersionCheckResponse = withContext(Dispatchers.IO) {
        val url = URL("$baseUrl/check_version.php")
        val connection = url.openConnection() as HttpURLConnection

        try {
            // Przygotuj dane POST
            val postData = "app_name=${URLEncoder.encode(appName, "UTF-8")}&current_version=${URLEncoder.encode(currentVersion, "UTF-8")}"

            connection.requestMethod = "POST"
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.setRequestProperty("User-Agent", "OplatyOgrodoweApp/1.0")
            connection.setRequestProperty("X-API-KEY", apiKey)
            connection.setRequestProperty("Content-Length", postData.length.toString())
            connection.connectTimeout = AppConfig.CONNECTION_TIMEOUT
            connection.readTimeout = AppConfig.READ_TIMEOUT

            // Wyślij dane
            val writer = OutputStreamWriter(connection.outputStream)
            writer.write(postData)
            writer.flush()
            writer.close()

            val responseCode = connection.responseCode
            when (responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val reader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
                    val response = reader.readText()
                    reader.close()

                    parseVersionCheckResponse(response)
                }
                HttpURLConnection.HTTP_UNAUTHORIZED -> {
                    throw Exception("Błąd autoryzacji - sprawdź API key")
                }
                HttpURLConnection.HTTP_BAD_REQUEST -> {
                    val errorStream = connection.errorStream
                    val errorMessage = if (errorStream != null) {
                        BufferedReader(InputStreamReader(errorStream, "UTF-8")).use {
                            val errorResponse = it.readText()
                            try {
                                val errorJson = JSONObject(errorResponse)
                                errorJson.getString("error")
                            } catch (e: Exception) {
                                errorResponse
                            }
                        }
                    } else {
                        "Błędne żądanie"
                    }
                    throw Exception(errorMessage)
                }
                HttpURLConnection.HTTP_NOT_FOUND -> {
                    throw Exception("Nie znaleziono aplikacji w systemie")
                }
                else -> {
                    throw Exception("HTTP Error: $responseCode")
                }
            }
        } catch (e: java.net.SocketTimeoutException) {
            throw Exception("Timeout - sprawdź połączenie internetowe")
        } catch (e: java.net.ConnectException) {
            throw Exception("Nie można połączyć się z serwerem")
        } catch (e: Exception) {
            if (e.message?.contains("HTTP Error") == true ||
                e.message?.contains("Błąd") == true ||
                e.message?.contains("Timeout") == true) {
                throw e
            }
            throw Exception("Błąd połączenia: ${e.message}")
        } finally {
            connection.disconnect()
        }
    }

    // Parsowanie odpowiedzi JSON
    private fun parseVersionCheckResponse(jsonString: String): VersionCheckResponse {
        try {
            val jsonObject = JSONObject(jsonString)

            return VersionCheckResponse(
                appName = jsonObject.getString("app_name"),
                currentVersion = jsonObject.getString("current_version"),
                latestVersion = jsonObject.getString("latest_version"),
                recommendedVersion = jsonObject.getString("recommended_version"),
                minimumVersion = jsonObject.getString("minimum_version"),
                needsUpdate = jsonObject.getBoolean("needs_update"),
                mustUpdate = jsonObject.getBoolean("must_update"),
                updateAvailable = jsonObject.getBoolean("update_available"),
                changelog = jsonObject.optString("changelog", "")
            )
        } catch (e: Exception) {
            throw Exception("Błąd parsowania odpowiedzi wersji: ${e.message}")
        }
    }
}