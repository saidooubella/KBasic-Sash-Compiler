package sash.tools

internal interface SeparatedList<E : Any, S : Any> {
    val sumSize: Int
    val elementsSize: Int
    fun getElement(index: Int): E
    fun isEmpty(): Boolean
}

internal interface MutableSeparatedList<E : Any, S : Any> : SeparatedList<E, S> {
    fun addElement(element: E)
    fun addSeparator(separator: S)
}

internal object EmptySeparatedList : SeparatedList<Nothing, Nothing> {
    override val sumSize get() = 0
    override val elementsSize get() = 0
    override fun getElement(index: Int) = throw UnsupportedOperationException()
    override fun isEmpty() = true
}

private class MutableSeparatedListImpl<E : Any, S : Any> : MutableSeparatedList<E, S> {

    private val elements = ArrayList<Any>()

    override val sumSize get() = elements.size

    override val elementsSize get() = (sumSize + 1) / 2

    override fun addElement(element: E) {
        elements += element
    }

    override fun addSeparator(separator: S) {
        elements += separator
    }

    @Suppress("UNCHECKED_CAST")
    override fun getElement(index: Int): E {
        return elements[index * 2] as E
    }

    override fun isEmpty() = elements.isEmpty()
}

internal fun <E : Any, S : Any> separatedListOf(): MutableSeparatedList<E, S> = MutableSeparatedListImpl()

internal fun <E : Any, S : Any> SeparatedList<E, S>.first() = getElement(0)

internal fun <E : Any, S : Any> SeparatedList<E, S>.last() = getElement(elementsSize - 1)

internal inline fun <E : Any, S : Any, R> SeparatedList<E, S>.map(action: (E) -> R): List<R> {
    return mutableListOf<R>().apply { this@map.forEach { this += action(it) } }
}

internal inline fun <E : Any, S : Any, R> SeparatedList<E, S>.mapIndexed(action: (Int, E) -> R): List<R> {
    return mutableListOf<R>().apply { this@mapIndexed.forEachIndexed { i, item -> this += action(i, item) } }
}

internal inline fun <E : Any, S : Any> SeparatedList<E, S>.forEach(action: (E) -> Unit) {
    for (index in 0 until elementsSize) action(getElement(index))
}

internal inline fun <E : Any, S : Any> SeparatedList<E, S>.forEachIndexed(action: (Int, E) -> Unit) {
    for (index in 0 until elementsSize) action(index, getElement(index))
}
