package systems.untangle.karta

import systems.untangle.karta.data.Px
import systems.untangle.karta.data.PxSize
import systems.untangle.karta.data.px
import systems.untangle.karta.data.times

actual val kartaTileSize: Px
    get() = 256.px.times(2)
actual val itemsSize: PxSize
    get() = PxSize(30.px.times(2), 48.px.times(2))