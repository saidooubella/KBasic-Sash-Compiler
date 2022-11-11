package sash.span

internal class PositionBuilder {

    private var line = 1
    private var column = 1
    private var index = 0

    internal fun advance(character: Char) {
        index += 1
        column += 1
        if (character == '\n') {
            line += 1
            column = 1
        }
    }

    internal fun build() = Position(line, column, index)
}
