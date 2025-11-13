package systems.untangle.karta

class DesktopLogger : Logger {
    override fun e(tag: String, message: String) = println(message)
    override fun i(tag: String, message: String) = println(message)
    override fun d(tag: String, message: String) = println(message)
}

actual fun getLogger() : Logger = DesktopLogger()
