package sash.input

import sash.input.providers.ReaderProvider
import sash.span.Position
import sash.span.PositionBuilder

internal class CharInput(provider: ReaderProvider) : AbstractInput<Int, Char>(provider) {
    private val position: PositionBuilder = PositionBuilder()
    override fun onPreNext(current: Char) = position.advance(current)
    internal fun position(): Position = position.build()
}
