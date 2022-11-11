package sash.input

internal interface InputProvider<I : Any, R : Any> : AutoCloseable {
    val emptyValue: R
    fun isEndReached(item: I): Boolean
    fun nextItem(): I
    fun map(item: I): R
}
