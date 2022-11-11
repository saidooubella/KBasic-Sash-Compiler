package sash.errors

internal data class ErrorReports(private val errors: List<ErrorMessage>) : Iterable<ErrorMessage> {

    internal val size: Int get() = errors.size

    internal val isEmpty: Boolean get() = errors.isEmpty()

    internal fun sortedWithIndex(): ErrorReports = ErrorReports(sortedWith(ErrorReports.INDICES_COMPARATOR))

    internal operator fun get(index: Int): ErrorMessage = errors[index]

    override fun iterator(): Iterator<ErrorMessage> = ErrorsIterator()

    private inner class ErrorsIterator : Iterator<ErrorMessage> {
        private var index: Int = 0
        override fun hasNext() = index < size
        override fun next() = errors[index++]
    }

    companion object {
        private val INDICES_COMPARATOR = Comparator<ErrorMessage> { left, right ->
            left.span.start.index.compareTo(right.span.start.index)
        }
    }
}