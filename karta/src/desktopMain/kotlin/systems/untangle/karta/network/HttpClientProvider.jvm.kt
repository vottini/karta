package systems.untangle.karta.network


// androidMain/kotlin/com/example/client/AndroidHttpClientProvider.kt
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import okhttp3.OkHttpClient
import java.security.SecureRandom
import java.security.cert.X509Certificate
import javax.net.ssl.*

actual class HttpClientProvider {
    actual fun getHttpClient(): HttpClient {

        return HttpClient(OkHttp) {

            engine {

                // Create a trust manager that does not validate certificate chains
                val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                    override fun checkClientTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun checkServerTrusted(
                        chain: Array<out X509Certificate>?,
                        authType: String?
                    ) {
                    }

                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
                })

                // Install the all-trusting trust manager
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, SecureRandom())

                // Create an ssl socket factory with our all-trusting manager
                val sslSocketFactory = sslContext.socketFactory


                config {
                    sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                    hostnameVerifier(NoopHostnameVerifier()) // Accept all hostnames
                }
            }
        }
    }
//
//  actual  fun getOkHttpClient(): OkHttpClient {
//        try {
//            // Create a trust manager that does not validate certificate chains
//            val trustAllCerts = arrayOf<TrustManager>(
//                object : X509TrustManager {
//                    override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
//                    override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
//                    override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
//                }
//            )
//
//            val sslContext = SSLContext.getInstance("SSL")
//            sslContext.init(null, trustAllCerts, SecureRandom())
//            val sslSocketFactory = sslContext.socketFactory
//
//            return OkHttpClient.Builder()
//                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
//                .hostnameVerifier { _, _ -> true } // Trust all hostnames
//                .build()
//        } catch (e: Exception) {
//            throw RuntimeException(e)
//        }
//    }

}

// A NoopHostnameVerifier is needed to skip hostname verification
class NoopHostnameVerifier : HostnameVerifier {
    override fun verify(hostname: String?, session: SSLSession?): Boolean = true
}
