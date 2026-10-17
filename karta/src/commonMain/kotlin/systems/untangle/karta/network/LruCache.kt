package systems.untangle.karta.network

internal class LruCache<K, V>(private val maxSize: Int) {
    private val map = LinkedHashMap<K, V>()
    private val accessQueue = ArrayDeque<K>()

    operator fun get(key: K): V? {
        val value = map[key] ?: return null
        accessQueue.remove(key)
        accessQueue.addLast(key)
        return value
    }

    fun peek(key: K): V? = map[key]

    operator fun set(key: K, value: V) {
        if (map.containsKey(key)) {
            accessQueue.remove(key)
        } else if (map.size >= maxSize) {
            map.remove(accessQueue.removeFirst())
        }
        map[key] = value
        accessQueue.addLast(key)
    }
}
