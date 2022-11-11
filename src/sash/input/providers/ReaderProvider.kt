package sash.input.providers

import sash.input.CharInput
import sash.input.InputProvider
import java.io.BufferedReader
import java.io.IOException
import java.io.Reader

internal class ReaderProvider(private val reader: BufferedReader) : InputProvider<Int, Char> {
    override val emptyValue: Char = '\u0000'
    override fun isEndReached(item: Int): Boolean = item == -1
    override fun nextItem(): Int = reader.safeRead()
    override fun map(item: Int): Char = item.toChar()
    override fun close() = reader.close()
}

internal operator fun CharInput.contains(text: String): Boolean {
    for ((index, char) in text.withIndex())
        if (peek(index) != char)
            return false
    return true
}

private fun Reader.safeRead() = try { read() } catch (e: IOException) { -1 }
