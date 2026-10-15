package systems.untangle.karta.network

/**
 * A source of XYZ map tiles. Either a remote [TileServer], a [LocalTileDirectory],
 * or a local [MBTilesSource] SQLite file.
 */
sealed interface TileSource
