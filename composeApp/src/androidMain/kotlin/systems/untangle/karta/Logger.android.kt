package systems.untangle.karta

import android.util.Log

class AndroidLogger : Logger {
    override fun e(tag: String, message: String) { Log.e(tag, message) }
    override fun i(tag: String, message: String) { Log.i(tag, message) }
    override fun d(tag: String, message: String) { Log.d(tag, message) }
}

actual fun getLogger() : Logger = AndroidLogger()
