package sash.span

internal data class Span(val start: Position, val end: Position) : Spannable {

    constructor(start: Spannable, end: Spannable) :
            this(start.span.start, end.span.end)

    override val span = this
}
