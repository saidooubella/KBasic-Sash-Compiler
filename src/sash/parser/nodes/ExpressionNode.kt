package sash.parser.nodes

import sash.lexer.token.Token
import sash.span.Span
import sash.span.Spannable
import sash.span.plus

internal sealed class ExpressionNode : Spannable

internal data class TernaryExpressionNode(
    val condition: ExpressionNode,
    val question: Token,
    val ifExpression: ExpressionNode,
    val colon: Token,
    val elseExpression: ExpressionNode
) : ExpressionNode() {
    override val span: Span = condition + elseExpression
}

internal data class CallExpressionNode(
    val target: ExpressionNode,
    val open: Token,
    val args: List<ExpressionNode>,
    val close: Token
) : ExpressionNode() {
    override val span: Span = target + close
}

internal data class LiteralExpressionNode(
    val token: Token,
    val value: Any,
    override val span: Span
) : ExpressionNode()

internal data class AssignmentExpressionNode(
    val target: ExpressionNode,
    val equal: Token,
    val expression: ExpressionNode
) : ExpressionNode() {
    override val span: Span = target + expression
}

internal data class VariableExpressionNode(
    val identifier: Token
) : ExpressionNode() {
    override val span: Span = identifier.span
}

internal data class UnaryOperationExpressionNode(
    val operation: Token,
    val operand: ExpressionNode
) : ExpressionNode() {
    override val span: Span = operation + operand
}

internal data class BinaryOperationExpressionNode(
    val left: ExpressionNode,
    val operation: Token,
    val right: ExpressionNode
) : ExpressionNode() {
    override val span: Span = left + right
}

internal data class ParenthesisedExpressionNode(
    val leftParent: Token,
    val expression: ExpressionNode,
    val rightParent: Token
) : ExpressionNode() {
    override val span: Span = leftParent + rightParent
}
