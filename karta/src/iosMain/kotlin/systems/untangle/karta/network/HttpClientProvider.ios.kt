package systems.untangle.karta.network
// iosMain/kotlin/com/example/client/IosHttpClientProvider.kt
import io.ktor.client.*
import io.ktor.client.engine.darwin.*
import platform.Foundation.NSURLAuthenticationChallenge
import platform.Foundation.NSURLSessionAuthChallengeDisposition
//import platform.Foundation.NSURLSessionAuthChallengePerformDisposition
//import platform.Foundation.trust

actual class HttpClientProvider {
    actual fun getHttpClient(): HttpClient {
        return HttpClient(Darwin) {
            engine {
//                handleChallenge { challenge: NSURLAuthenticationChallenge, completionHandler: (NSURLSessionAuthChallengeDisposition, NSURLCredential?) -> Unit ->
//                    // Your logic to trust the self-signed certificate goes here.
//                    // A common approach is to extract the server's certificate and set it as the anchor.
//
//                    // WARNING: This example accepts any challenge and is highly insecure.
//                    // You should implement proper certificate handling/pinning here.
//                    completionHandler(
//                        NSURLSessionAuthChallengePerformDisposition.NSURLSessionAuthChallengePerformDefault,
//                        null
//                    )
//                }
            }
        }
    }
}
