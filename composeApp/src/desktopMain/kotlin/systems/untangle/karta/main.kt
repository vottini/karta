package systems.untangle.karta

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

import coil3.ImageLoader
import coil3.SingletonImageLoader
import coil3.util.DebugLogger

fun main() = application {
	SingletonImageLoader.setSafe { context ->
		ImageLoader.Builder(context)
			.logger(DebugLogger())
			.build()
	}

	Window(
		onCloseRequest = ::exitApplication,
		title = "Karta",
	) {
		App()
	}
}
