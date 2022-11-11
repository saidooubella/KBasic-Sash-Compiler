@file:Suppress("NOTHING_TO_INLINE")

package sash.tools

internal inline fun <T> Array<out T>.allIndexed(predicate: (Int, T) -> Boolean): Boolean {
    for ((index, element) in withIndex()) if (!predicate(index, element)) return false
    return true
}

internal inline fun <A, B> List<A>.zipAll(other: List<B>, predicate: (A, B) -> Boolean): Boolean {
    if (other.size != size) return false
    for (index in 0 until size)
        if (!predicate(this[index], other[index]))
            return false
    return true
}

internal inline fun <K, V> List<Pair<K, V>>.toMutableMap(): MutableMap<K, V> {
    val destination: MutableMap<K, V> = hashMapOf()
    forEach { destination[it.first] = it.second }
    return destination
}

internal inline fun <V> List<V>.toMutableSet(): MutableSet<V> {
    val destination: MutableSet<V> = hashSetOf()
    forEach { destination += it }
    return destination
}

internal inline fun <K, V> MutableMap<K, V>.putValue(key: K, value: V) {
    require(key !in this)
    put(key, value)
}
