package sash.lexer.token

import sash.span.Position
import sash.span.Span

internal data class TokenInfo(val text: String, val type: TokenType, val value: Any?, val span: Span) {
    companion object {
        internal val Empty = run {
            val span = Position.Empty.run { Span(this, this) }
            TokenInfo("End of file", TokenType.EndOfFile, null, span)
        }
    }
}
