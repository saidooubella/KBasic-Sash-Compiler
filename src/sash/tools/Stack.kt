package sash.tools

internal interface Stack<T> {
    fun push(element: T)
    fun pop(): T
    fun peek(): T
    fun get(index: Int): T
    fun isEmpty(): Boolean
    fun size(): Int
}

private class StackImpl<T : Any>(private var elements: Array<T?>) : Stack<T> {

    private var size = 0

    override fun push(element: T) {
        ensureCapacity()
        elements[size++] = element
    }

    override fun pop(): T {
        ensureNotEmpty()
        val temp = elements[--size]
        elements[size] = null
        return temp!!
    }

    override fun peek(): T {
        ensureNotEmpty()
        return elements[size - 1]!!
    }

    override fun get(index: Int): T {
        ensureIndexInBounds(index)
        return elements[index]!!
    }

    override fun size(): Int = size

    override fun isEmpty(): Boolean = size == 0

    private fun ensureIndexInBounds(index: Int) {
        require(size != 0 && 0 <= index && index < size) { "The index out of bounds" }
    }

    private fun ensureNotEmpty() {
        check(size != 0) { "The stack is empty" }
    }

    private fun ensureCapacity() {
        if (size == elements.size) {
            val newCapacity = (elements.size + 1) shl 1
            check(newCapacity >= 0) { "The stack is too big" }
            elements = elements.copyOf(newCapacity)
        }
    }
}

internal inline fun <T> Stack<T>.forEachReversed(action: (T) -> Unit) {
    for (index in size() - 1 downTo 0) action(get(index))
}

@Suppress("NOTHING_TO_INLINE")
internal inline fun <T> Stack<T>.peekOrNull() = if (isEmpty()) null else peek()

@Suppress("UNCHECKED_CAST")
internal fun <T : Any> stackOf(size: Int = 16): Stack<T> {
    return StackImpl(arrayOfNulls<Any?>(size) as Array<T?>)
}
