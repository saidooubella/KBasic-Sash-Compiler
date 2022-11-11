package sash.binder.nodes

import sash.lexer.token.TokenType
import sash.types.*

private val OPERATIONS = mapOf(
    /////////////////////////////////////////////////////////////////////////////////

    UnaryOperationKey(TokenType.Plus, IntType) to
            UnaryOperationValue(UnaryOperation.Identity, IntType),
    UnaryOperationKey(TokenType.Plus, DoubleType) to
            UnaryOperationValue(UnaryOperation.Identity, DoubleType),
    UnaryOperationKey(TokenType.Plus, FloatType) to
            UnaryOperationValue(UnaryOperation.Identity, FloatType),
    UnaryOperationKey(TokenType.Plus, LongType) to
            UnaryOperationValue(UnaryOperation.Identity, LongType),

    /////////////////////////////////////////////////////////////////////////////////

    UnaryOperationKey(TokenType.Minus, IntType) to
            UnaryOperationValue(UnaryOperation.Negation, IntType),
    UnaryOperationKey(TokenType.Minus, DoubleType) to
            UnaryOperationValue(UnaryOperation.Negation, DoubleType),
    UnaryOperationKey(TokenType.Minus, FloatType) to
            UnaryOperationValue(UnaryOperation.Negation, FloatType),
    UnaryOperationKey(TokenType.Minus, LongType) to
            UnaryOperationValue(UnaryOperation.Negation, LongType),

    /////////////////////////////////////////////////////////////////////////////////

    UnaryOperationKey(TokenType.Bang, BooleanType) to
            UnaryOperationValue(UnaryOperation.LogicalNegation, BooleanType)

    /////////////////////////////////////////////////////////////////////////////////
)

@Suppress("FunctionName")
internal fun UnaryOperationBinder(operationToken: TokenType, operandType: AbstractType) =
    OPERATIONS[UnaryOperationKey(operationToken, operandType)]

private data class UnaryOperationKey(
    private val operationToken: TokenType,
    private val operandType: AbstractType
)

internal data class UnaryOperationValue(
    internal val operation: UnaryOperation,
    internal val operationType: AbstractType
)

internal sealed class UnaryOperation {

    override fun toString(): String = when (this) {
        LogicalNegation -> "!"
        Identity -> "+"
        Negation -> "-"
    }

    internal object LogicalNegation : UnaryOperation()
    internal object Identity : UnaryOperation()
    internal object Negation : UnaryOperation()
}