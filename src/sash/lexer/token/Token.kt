package sash.lexer.token

import sash.span.Position
import sash.span.Span
import sash.span.Spannable

internal data class Token(
    internal val text: String,
    internal val type: TokenType,
    internal val value: Any?,
    override val span: Span,
    internal val leading: List<Trivia>,
    internal val trailing: List<Trivia>
) : Spannable {

    internal fun requireValue(): Any = value ?: throw IllegalStateException()

    companion object {
        internal val Empty = run {
            val span = Position.Empty.run { Span(this, this) }
            Token("End of file", TokenType.EndOfFile, null, span, listOf(), listOf())
        }
    }
}