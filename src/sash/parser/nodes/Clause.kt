package sash.parser.nodes

import sash.lexer.token.Token
import sash.span.Span
import sash.span.Spannable
import sash.span.plus

internal sealed class Clause : Spannable

internal data class TypeClause(val colon: Token, val type: ValueType) : Clause() {
    override val span: Span = colon + type
}

internal data class ElseClause(val keyword: Token, val block: StatementNode) : Clause() {
    override val span: Span = keyword + block
}

internal data class ParamClause(val identifier: Token, val type: TypeClause) : Clause() {
    override val span: Span = identifier + type
}
