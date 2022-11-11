package sash.emitter.scopes

import sash.emitter.consts.USHORT_MAX_VALUE
import sash.symbols.Symbol

internal class GlobalScope {

    private val globals = hashMapOf<Symbol, Int>()

    internal val size: Int get() = globals.size

    internal fun putGlobal(symbol: Symbol): Int {
        return size.also {
            check(it <= USHORT_MAX_VALUE)
            globals[symbol] = it
        }
    }

    internal fun getGlobal(symbol: Symbol): Int? = globals[symbol] ?: error("Not found")
}