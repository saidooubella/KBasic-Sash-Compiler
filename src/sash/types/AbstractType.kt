package sash.types

internal abstract class AbstractType(val name: String) {
    abstract fun assignableTo(that: AbstractType): Boolean
    abstract override fun equals(other: Any?): Boolean
    abstract override fun hashCode(): Int
    final override fun toString(): String = name
}
