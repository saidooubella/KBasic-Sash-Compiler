@file:Suppress("NOTHING_TO_INLINE")

package sash.emitter.tools

import java.io.OutputStream
import kotlin.math.max

internal class BufferArray(capacity: Int = 32) {

    private var _buffer = ByteArray(capacity)
    private var _size = 0

    private val buffer: ByteArray
        get() = _buffer

    val size: Int
        get() = _size

    internal fun appendByte(value: Byte): Int {
        ensureSpace(1)
        buffer.write1Byte(size, value)
        return increaseSize(1)
    }

    internal fun appendUByte(value: Int): Int {
        if (!isUByte(value)) error("Value (over/under)flow")
        ensureSpace(1)
        buffer.write1Byte(size, value.toByte())
        return increaseSize(1)
    }

    internal fun appendUShort(value: Int): Int {
        if (!isUShort(value)) error("Value (over/under)flow")
        ensureSpace(2)
        buffer.write2Byte(size, value.toShort())
        return increaseSize(2)
    }

    internal fun appendShort(value: Int): Int {
        if (!isShort(value)) error("Value (over/under)flow")
        ensureSpace(2)
        buffer.write2Byte(size, value.toShort())
        return increaseSize(2)
    }

    internal fun appendInt(value: Int): Int {
        ensureSpace(4)
        buffer.write4Byte(size, value)
        return increaseSize(4)
    }

    internal fun appendLong(value: Long): Int {
        ensureSpace(8)
        buffer.write8Byte(size, value)
        return increaseSize(8)
    }

    internal fun appendBytes(bytes: ByteArray) {
        val bytesSize = bytes.size
        ensureSpace(bytesSize)
        System.arraycopy(bytes, 0, buffer, size, bytesSize)
        increaseSize(bytesSize)
    }

    internal fun setShort(index: Int, value: Int) {
        if (!isShort(value)) error("Value (over/under)flow")
        buffer.write2Byte(checkBounds(index), value.toShort())
    }

    internal fun setUShort(index: Int, value: Int) {
        if (!isUShort(value)) error("Value (over/under)flow")
        buffer.write2Byte(checkBounds(index), value.toShort())
    }

    internal fun writeTo(output: OutputStream) {
        output.write(buffer, 0, size)
        output.flush()
    }

    private fun ensureSpace(space: Int) {
        val newSize = size + space
        if (newSize >= buffer.size) {
            val newCapacity = max(buffer.size shl 1, newSize)
            _buffer = buffer.copyOf(newCapacity)
        }
    }

    private inline fun increaseSize(value: Int) = size.also { _size += value }
}

private inline fun isShort(value: Int) = -32768 <= value && value <= 32767

private inline fun isUShort(value: Int) = value in 0..65535

private inline fun isUByte(value: Int) = value in 0..255

private inline fun BufferArray.checkBounds(index: Int): Int {
    return require(index in 0 until size) { "Index out of bounds" }.let { index }
}
