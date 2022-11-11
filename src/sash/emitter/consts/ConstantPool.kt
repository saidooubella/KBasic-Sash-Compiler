package sash.emitter.consts

import sash.emitter.tools.BufferArray
import java.io.OutputStream

internal const val USHORT_MAX_VALUE = 65535

internal class ConstantPool {

    private val existent = hashMapOf<Any, Int>()
    private val buffer = BufferArray()
    private var size = 0

    init {
        buffer.appendUShort(USHORT_MAX_VALUE)
    }

    fun writeTo(output: OutputStream) {
        buffer.setUShort(0, size)
        buffer.writeTo(output)
    }

    fun addConstant(value: Any): Int {

        val index = existent[value]
        if (index != null) return index

        if (size > USHORT_MAX_VALUE)
            throw IllegalStateException()

        when (value) {
            is Int -> {
                buffer.appendByte(ConstsTags.TAG_INTEGER_CONST)
                buffer.appendInt(value)
            }
            is Long -> {
                buffer.appendByte(ConstsTags.TAG_LONG_CONST)
                buffer.appendLong(value)
            }
            is Float -> {
                buffer.appendByte(ConstsTags.TAG_FLOAT_CONST)
                buffer.appendInt(value.toBits())
            }
            is Double -> {
                buffer.appendByte(ConstsTags.TAG_DOUBLE_CONST)
                buffer.appendLong(value.toBits())
            }
            is String -> {
                val bytes = value.toByteArray(Charsets.UTF_8)
                buffer.appendByte(ConstsTags.TAG_STRING_CONST)
                buffer.appendUShort(bytes.size)
                buffer.appendBytes(bytes)
            }
            is FunctionPtr -> {
                buffer.appendByte(ConstsTags.TAG_FUN_CONST)
                buffer.appendUShort(value.pointer)
                buffer.appendUByte(value.arity)
            }
            else -> throw IllegalArgumentException()
        }

        existent[value] = size++

        return size - 1
    }
}

internal data class FunctionPtr(val arity: Int, val pointer: Int)