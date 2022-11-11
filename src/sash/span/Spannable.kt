package sash.span

internal interface Spannable {
    val span: Span
}

internal operator fun Spannable.plus(that: Spannable): Span = Span(this, that)