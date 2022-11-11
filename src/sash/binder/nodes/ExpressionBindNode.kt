package sash.binder.nodes

import sash.span.Span
import sash.span.Spannable
import sash.symbols.*
import sash.types.*

internal sealed class ExpressionBindNode : Spannable {
    internal abstract val type: AbstractType
    internal abstract val isValidStatement: Boolean
    internal fun isError(): Boolean = type == ErrorType
}

internal data class CallExpressionBindNode(
    internal val target: ExpressionBindNode,
    internal val arguments: List<ExpressionBindNode>,
    override val type: AbstractType,
    override val span: Span
) : ExpressionBindNode() {
    override val isValidStatement: Boolean = true
}

internal data class TernaryExpressionBindNode(
    internal val condition: ExpressionBindNode,
    internal val ifExpression: ExpressionBindNode,
    internal val elseExpression: ExpressionBindNode,
    override val type: AbstractType,
    override val span: Span
) : ExpressionBindNode() {
    override val isValidStatement: Boolean = false
}

internal data class AssignmentExpressionBindNode(
    internal val variable: VariableSymbol,
    internal val value: ExpressionBindNode,
    override val type: AbstractType,
    override val span: Span
) : ExpressionBindNode() {
    override val isValidStatement: Boolean = true
}

internal data class VariableExpressionBindNode(
    internal val symbol: Symbol,
    override val span: Span
) : ExpressionBindNode() {
    override val type: AbstractType = symbol.type
    override val isValidStatement: Boolean = false
}

internal data class BinaryOperationExpressionBindNode(
    internal val left: ExpressionBindNode,
    internal val operation: BinaryOperation,
    internal val right: ExpressionBindNode,
    override val type: AbstractType,
    override val span: Span
) : ExpressionBindNode() {
    override val isValidStatement: Boolean = false
}

internal data class UnaryOperationExpressionBindNode(
    internal val operation: UnaryOperation,
    internal val operand: ExpressionBindNode,
    override val type: AbstractType,
    override val span: Span
) : ExpressionBindNode() {
    override val isValidStatement: Boolean = false
}

internal data class ParenthesisedExpressionBindNode(
    internal val expression: ExpressionBindNode,
    override val span: Span
) : ExpressionBindNode() {
    override val type: AbstractType = expression.type
    override val isValidStatement: Boolean = false
}

internal data class LiteralExpressionBindNode(
    internal val value: Any,
    override val type: PrimitiveType,
    override val span: Span
) : ExpressionBindNode() {
    override val isValidStatement: Boolean = false
}

internal data class ErrorExpressionBindNode(
    override val span: Span
) : ExpressionBindNode() {
    override val type: AbstractType = ErrorType
    override val isValidStatement: Boolean = true
}
