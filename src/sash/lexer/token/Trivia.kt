package sash.lexer.token

import sash.span.Span
import sash.span.Spannable

internal data class Trivia(
    internal val text: String,
    internal val type: TokenType,
    override val span: Span
) : Spannable