package systems.untangle.karta

import androidx.compose.ui.window.ComposeUIViewController


class iOSLogger : Logger {
    override fun e(tag: String, message: String) = println(message)
    override fun i(tag: String, message: String) = println(message)
    override fun d(tag: String, message: String) = println(message)
}

actual fun getLogger() : Logger = iOSLogger()
//fun MainViewController() = ComposeUIViewController { iOSLogger() }