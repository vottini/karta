package systems.untangle.karta.network

import io.ktor.client.HttpClient

// commonMain/kotlin/com/example/client/HttpClientProvider.kt
expect class HttpClientProvider() {
    fun getHttpClient(): HttpClient
}
