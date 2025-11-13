package systems.untangle.karta

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
