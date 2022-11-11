package sash.span

internal data class Position(val line: Int, val column: Int, val index: Int) {
    companion object {
        internal val Empty = Position(1, 1, 0)
    }
}
