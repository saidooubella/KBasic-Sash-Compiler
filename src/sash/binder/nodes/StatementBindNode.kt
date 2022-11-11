package sash.binder.nodes

import sash.span.Span
import sash.span.Spannable
import sash.symbols.FunctionSymbol
import sash.symbols.VariableSymbol

internal sealed class StatementBindNode : Spannable

internal data class FunctionStatementBindNode(
    internal val function: FunctionSymbol,
    internal val block: BlockStatementBindNode,
    internal val shouldInsertReturn: Boolean,
    override val span: Span
) : StatementBindNode()

internal data class VariableStatementBindNode(
    internal val variable: VariableSymbol,
    internal val value: ExpressionBindNode,
    override val span: Span
) : StatementBindNode()

internal data class ReturnStatementBindNode(
    internal val value: ExpressionBindNode?,
    override val span: Span
) : StatementBindNode()

internal data class ContinueStatementBindNode(
    override val span: Span
) : StatementBindNode()

internal data class BreakStatementBindNode(
    override val span: Span
) : StatementBindNode()

internal data class PrintStatementBindNode(
    val expression: ExpressionBindNode,
    override val span: Span
) : StatementBindNode()

internal data class ExpressionStatementBindNode(
    internal val expression: ExpressionBindNode
) : StatementBindNode() {
    override val span: Span = expression.span
}

internal data class BlockStatementBindNode(
    internal val statements: List<StatementBindNode>,
    override val span: Span
) : StatementBindNode()

internal data class IfStatementBindNode(
    internal val condition: ExpressionBindNode,
    internal val ifBlock: StatementBindNode,
    internal val elseBlock: StatementBindNode?,
    override val span: Span
) : StatementBindNode()

internal data class WhileStatementBindNode(
    internal val condition: ExpressionBindNode,
    internal val block: StatementBindNode,
    override val span: Span
) : StatementBindNode()

internal data class DoWhileStatementBindNode(
    internal val block: StatementBindNode,
    internal val condition: ExpressionBindNode,
    override val span: Span
) : StatementBindNode()

