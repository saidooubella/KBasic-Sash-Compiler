package sash.parser.nodes

import sash.lexer.token.Token
import sash.span.Span
import sash.span.Spannable

internal sealed class ValueType : Spannable {
    internal data class Normal(
        internal val type: Token
    ) : ValueType() {
        override val span: Span = type.span
    }
}
