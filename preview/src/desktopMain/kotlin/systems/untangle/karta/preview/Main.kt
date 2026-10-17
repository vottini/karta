package systems.untangle.karta.preview

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import kotlinx.coroutines.runBlocking
import systems.untangle.karta.Karta
import systems.untangle.karta.data.Coordinates
import systems.untangle.karta.network.LocalTileDirectory
import systems.untangle.karta.network.MBTilesSource
import systems.untangle.karta.network.TileServer
import systems.untangle.karta.network.TileSource
import systems.untangle.karta.network.readMBTilesMetadata
import kotlin.system.exitProcess

private data class ViewConfig(
    val tileSource: TileSource,
    val initialCoords: Coordinates,
    val initialZoom: Int,
    val minZoom: Int = 2,
    val maxZoom: Int = 19
)

fun main(args: Array<String>) {
    val config = parseArgs(args) ?: run {
        System.err.println("""
            Usage: preview <option> <value>
              --server    <url>   Tile server URL with {zoom}, {x}, {y} placeholders
              --directory <path>  Local tile directory (zoom/x/y.png layout)
              --mbtile    <path>  Local MBTiles SQLite file
        """.trimIndent())
        exitProcess(1)
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Karta Preview",
            state = WindowState(size = DpSize(1024.dp, 768.dp))
        ) {
            Karta(
                tileSource = config.tileSource,
                initialCoords = config.initialCoords,
                initialZoom = config.initialZoom,
                minZoom = config.minZoom,
                maxZoom = config.maxZoom
            )
        }
    }
}

private fun parseArgs(args: Array<String>): ViewConfig? {
    val argMap = mutableMapOf<String, String>()
    var i = 0
    while (i < args.size - 1) {
        if (args[i].startsWith("--")) {
            argMap[args[i]] = args[i + 1]
            i += 2
        } else {
            i++
        }
    }

    return when {
        "--server" in argMap -> ViewConfig(
            tileSource = TileServer(argMap["--server"]!!),
            initialCoords = Coordinates(0.0, 0.0),
            initialZoom = 2
        )
        "--directory" in argMap -> ViewConfig(
            tileSource = LocalTileDirectory(path = argMap["--directory"]!!),
            initialCoords = Coordinates(0.0, 0.0),
            initialZoom = 2
        )
        "--mbtile" in argMap -> {
            val path = argMap["--mbtile"]!!
            val metadata = runBlocking { readMBTilesMetadata(path) }

            if (metadata != null) {
                println("Name        : ${metadata.name ?: "-"}")
                println("Description : ${metadata.description ?: "-"}")
                println("Format      : ${metadata.format}")
                println("Type        : ${metadata.type ?: "-"}")
                println("Zoom range  : ${metadata.minZoom}–${metadata.maxZoom}")
                println("Bounds      : ${metadata.bounds.topLeft.longitude},${metadata.bounds.bottomRight.latitude}," +
                        "${metadata.bounds.bottomRight.longitude},${metadata.bounds.topLeft.latitude}")
                metadata.center?.let { println("Center      : ${it.latitude}, ${it.longitude}") }
                metadata.generator?.let { println("Generator   : $it") }
            }

            if (metadata != null && !metadata.format.isRaster) {
                System.err.println("\nError: format '${metadata.format}' is not a raster format — Karta cannot render vector tiles.")
                exitProcess(1)
            }

            val center = metadata?.center
                ?: metadata?.bounds?.let {
                    Coordinates(
                        latitude = (it.topLeft.latitude + it.bottomRight.latitude) / 2.0,
                        longitude = (it.topLeft.longitude + it.bottomRight.longitude) / 2.0
                    )
                }
                ?: Coordinates(0.0, 0.0)
            ViewConfig(
                tileSource = MBTilesSource(path = path),
                initialCoords = center,
                initialZoom = metadata?.minZoom ?: 2,
                minZoom = metadata?.minZoom ?: 2,
                maxZoom = metadata?.maxZoom ?: 19
            )
        }
        else -> null
    }
}
