package sash.binder.nodes

import sash.lexer.token.TokenType
import sash.types.*

@Suppress("FunctionName")
internal fun BinaryOperationBinder(left: AbstractType, operationToken: TokenType, right: AbstractType): BinaryOperationValue? {
    return MAP_OPERATIONS[BinaryOperationKey(left, operationToken, right)]
}

private val MAP_OPERATIONS = mapOf(
    //////////////////////////////////////////////////

    BinaryOperationKey(IntType, TokenType.Plus, IntType) to
            BinaryOperationValue(BinaryOperation.Addition, IntType),
    BinaryOperationKey(FloatType, TokenType.Plus, FloatType) to
            BinaryOperationValue(BinaryOperation.Addition, FloatType),
    BinaryOperationKey(LongType, TokenType.Plus, LongType) to
            BinaryOperationValue(BinaryOperation.Addition, LongType),
    BinaryOperationKey(DoubleType, TokenType.Plus, DoubleType) to
            BinaryOperationValue(BinaryOperation.Addition, DoubleType),

    //////////////////////////////////////////////////

    BinaryOperationKey(IntType, TokenType.Minus, IntType) to
            BinaryOperationValue(BinaryOperation.Subtraction, IntType),
    BinaryOperationKey(FloatType, TokenType.Minus, FloatType) to
            BinaryOperationValue(BinaryOperation.Subtraction, FloatType),
    BinaryOperationKey(LongType, TokenType.Minus, LongType) to
            BinaryOperationValue(BinaryOperation.Subtraction, LongType),
    BinaryOperationKey(DoubleType, TokenType.Minus, DoubleType) to
            BinaryOperationValue(BinaryOperation.Subtraction, DoubleType),

    //////////////////////////////////////////////////

    BinaryOperationKey(IntType, TokenType.Slash, IntType) to
            BinaryOperationValue(BinaryOperation.Division, IntType),
    BinaryOperationKey(FloatType, TokenType.Slash, FloatType) to
            BinaryOperationValue(BinaryOperation.Division, FloatType),
    BinaryOperationKey(LongType, TokenType.Slash, LongType) to
            BinaryOperationValue(BinaryOperation.Division, LongType),
    BinaryOperationKey(DoubleType, TokenType.Slash, DoubleType) to
            BinaryOperationValue(BinaryOperation.Division, DoubleType),

    //////////////////////////////////////////////////

    BinaryOperationKey(IntType, TokenType.Star, IntType) to
            BinaryOperationValue(BinaryOperation.Multiplication, IntType),
    BinaryOperationKey(FloatType, TokenType.Star, FloatType) to
            BinaryOperationValue(BinaryOperation.Multiplication, FloatType),
    BinaryOperationKey(LongType, TokenType.Star, LongType) to
            BinaryOperationValue(BinaryOperation.Multiplication, LongType),
    BinaryOperationKey(DoubleType, TokenType.Star, DoubleType) to
            BinaryOperationValue(BinaryOperation.Multiplication, DoubleType),

    //////////////////////////////////////////////////

    BinaryOperationKey(IntType, TokenType.GreaterThanEqual, IntType) to
            BinaryOperationValue(BinaryOperation.GreaterThanEqual, BooleanType),
    BinaryOperationKey(FloatType, TokenType.GreaterThanEqual, FloatType) to
            BinaryOperationValue(BinaryOperation.GreaterThanEqual, BooleanType),
    BinaryOperationKey(LongType, TokenType.GreaterThanEqual, LongType) to
            BinaryOperationValue(BinaryOperation.GreaterThanEqual, BooleanType),
    BinaryOperationKey(DoubleType, TokenType.GreaterThanEqual, DoubleType) to
            BinaryOperationValue(BinaryOperation.GreaterThanEqual, BooleanType),

    //////////////////////////////////////////////////

    BinaryOperationKey(IntType, TokenType.LessThanEqual, IntType) to
            BinaryOperationValue(BinaryOperation.LessThanEqual, BooleanType),
    BinaryOperationKey(FloatType, TokenType.LessThanEqual, FloatType) to
            BinaryOperationValue(BinaryOperation.LessThanEqual, BooleanType),
    BinaryOperationKey(LongType, TokenType.LessThanEqual, LongType) to
            BinaryOperationValue(BinaryOperation.LessThanEqual, BooleanType),
    BinaryOperationKey(DoubleType, TokenType.LessThanEqual, DoubleType) to
            BinaryOperationValue(BinaryOperation.LessThanEqual, BooleanType),

    //////////////////////////////////////////////////

    BinaryOperationKey(IntType, TokenType.GreaterThan, IntType) to
            BinaryOperationValue(BinaryOperation.GreaterThan, BooleanType),
    BinaryOperationKey(FloatType, TokenType.GreaterThan, FloatType) to
            BinaryOperationValue(BinaryOperation.GreaterThan, BooleanType),
    BinaryOperationKey(LongType, TokenType.GreaterThan, LongType) to
            BinaryOperationValue(BinaryOperation.GreaterThan, BooleanType),
    BinaryOperationKey(DoubleType, TokenType.GreaterThan, DoubleType) to
            BinaryOperationValue(BinaryOperation.GreaterThan, BooleanType),

    //////////////////////////////////////////////////

    BinaryOperationKey(IntType, TokenType.LessThan, IntType) to
            BinaryOperationValue(BinaryOperation.LessThan, BooleanType),
    BinaryOperationKey(FloatType, TokenType.LessThan, FloatType) to
            BinaryOperationValue(BinaryOperation.LessThan, BooleanType),
    BinaryOperationKey(LongType, TokenType.LessThan, LongType) to
            BinaryOperationValue(BinaryOperation.LessThan, BooleanType),
    BinaryOperationKey(DoubleType, TokenType.LessThan, DoubleType) to
            BinaryOperationValue(BinaryOperation.LessThan, BooleanType),

    //////////////////////////////////////////////////

    BinaryOperationKey(BooleanType, TokenType.PipePipe, BooleanType) to
            BinaryOperationValue(BinaryOperation.LogicalOr, BooleanType),
    BinaryOperationKey(BooleanType, TokenType.AmpersandAmpersand, BooleanType) to
            BinaryOperationValue(BinaryOperation.LogicalAnd, BooleanType),

    //////////////////////////////////////////////////

    BinaryOperationKey(IntType, TokenType.EqualEqual, IntType) to
            BinaryOperationValue(BinaryOperation.Equals, BooleanType),
    BinaryOperationKey(DoubleType, TokenType.EqualEqual, DoubleType) to
            BinaryOperationValue(BinaryOperation.Equals, BooleanType),
    BinaryOperationKey(BooleanType, TokenType.EqualEqual, BooleanType) to
            BinaryOperationValue(BinaryOperation.Equals, BooleanType),
    BinaryOperationKey(FloatType, TokenType.EqualEqual, FloatType) to
            BinaryOperationValue(BinaryOperation.Equals, BooleanType),
    BinaryOperationKey(LongType, TokenType.EqualEqual, LongType) to
            BinaryOperationValue(BinaryOperation.Equals, BooleanType),
    BinaryOperationKey(StringType, TokenType.EqualEqual, StringType) to
            BinaryOperationValue(BinaryOperation.Equals, BooleanType),
    BinaryOperationKey(CharType, TokenType.EqualEqual, CharType) to
            BinaryOperationValue(BinaryOperation.Equals, BooleanType),

    //////////////////////////////////////////////////

    BinaryOperationKey(IntType, TokenType.BangEqual, IntType) to
            BinaryOperationValue(BinaryOperation.NotEquals, BooleanType),
    BinaryOperationKey(DoubleType, TokenType.BangEqual, DoubleType) to
            BinaryOperationValue(BinaryOperation.NotEquals, BooleanType),
    BinaryOperationKey(BooleanType, TokenType.BangEqual, BooleanType) to
            BinaryOperationValue(BinaryOperation.NotEquals, BooleanType),
    BinaryOperationKey(FloatType, TokenType.BangEqual, FloatType) to
            BinaryOperationValue(BinaryOperation.NotEquals, BooleanType),
    BinaryOperationKey(LongType, TokenType.BangEqual, LongType) to
            BinaryOperationValue(BinaryOperation.NotEquals, BooleanType),
    BinaryOperationKey(StringType, TokenType.BangEqual, StringType) to
            BinaryOperationValue(BinaryOperation.NotEquals, BooleanType),
    BinaryOperationKey(CharType, TokenType.BangEqual, CharType) to
            BinaryOperationValue(BinaryOperation.NotEquals, BooleanType),

    //////////////////////////////////////////////////

    BinaryOperationKey(StringType, TokenType.Plus, StringType) to
        BinaryOperationValue(BinaryOperation.Concat, StringType)

    //////////////////////////////////////////////////
)

private data class BinaryOperationKey(
    private val left: AbstractType,
    private val operationToken: TokenType,
    private val right: AbstractType
)

internal data class BinaryOperationValue(
    val operation: BinaryOperation,
    val operationType: AbstractType
)

internal sealed class BinaryOperation {

    override fun toString(): String = when (this) {
        Addition, Concat -> "+"
        Subtraction -> "-"
        Multiplication -> "*"
        Division -> "/"
        LogicalOr -> "||"
        LogicalAnd -> "&&"
        Equals -> "=="
        NotEquals -> "!="
        GreaterThanEqual -> ">="
        LessThanEqual -> "<="
        GreaterThan -> ">"
        LessThan -> "<"
    }

    internal object Addition : BinaryOperation()
    internal object Subtraction : BinaryOperation()
    internal object Multiplication : BinaryOperation()
    internal object Division : BinaryOperation()
    internal object LogicalOr : BinaryOperation()
    internal object LogicalAnd : BinaryOperation()
    internal object Equals : BinaryOperation()
    internal object NotEquals : BinaryOperation()
    internal object Concat : BinaryOperation()
    internal object GreaterThanEqual : BinaryOperation()
    internal object LessThanEqual : BinaryOperation()
    internal object GreaterThan : BinaryOperation()
    internal object LessThan : BinaryOperation()
}
