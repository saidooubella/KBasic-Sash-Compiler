package sash.types

internal sealed class PrimitiveType(name: String) : AbstractType(name) {

    override fun assignableTo(that: AbstractType): Boolean = when {
        this is ErrorType || that is ErrorType || that is AnyType -> true
        else -> this == that
    }

    final override fun equals(other: Any?) = other is AbstractType && name == other.name
    final override fun hashCode(): Int = 31 + name.hashCode()
}

internal object BooleanType : PrimitiveType("Boolean")

internal object DoubleType : PrimitiveType("Double")

internal object StringType : PrimitiveType("String")

internal object FloatType : PrimitiveType("Float")

internal object CharType : PrimitiveType("Char")

internal object LongType : PrimitiveType("Long")

internal object AnyType : PrimitiveType("Any")

internal object IntType : PrimitiveType("Int")

internal object UnitType : PrimitiveType("Unit")

internal object ErrorType : PrimitiveType("???")
