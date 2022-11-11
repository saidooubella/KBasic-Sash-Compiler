@file:Suppress("NOTHING_TO_INLINE")

package sash.emitter.scopes

import sash.emitter.consts.USHORT_MAX_VALUE
import sash.symbols.Symbol
import java.util.*
import kotlin.collections.ArrayList

private inline fun <T> @Suppress("unused") T.unit() = Unit

internal class LocalScope(val parent: LocalScope? = null) {

    private val scopes = ArrayDeque<MutableMap<Symbol, Local>>()
    private val freeSymbols = ArrayList<FreeSymbol>()

    private inline fun fullSize(): Int = scopes.sumBy { it.size }

    internal inline fun freeSymbols(): List<FreeSymbol> = freeSymbols

    internal inline fun currentLocals(): Collection<Local> =
        scopes.peek().values.sortedWith(Comparator { o1, o2 -> o2.index.compareTo(o1.index) })

    internal inline fun isGlobal() = parent == null && scopes.isEmpty()

    internal inline fun startScope() = scopes.push(hashMapOf())

    internal inline fun endScope() = scopes.pop().unit()

    internal fun putLocal(symbol: Symbol) {
        val size = fullSize()
        if (size > USHORT_MAX_VALUE)
            throw IllegalStateException()
        scopes.peek()[symbol] = Local(size, false)
    }

    internal fun getLocal(symbol: Symbol): Local? {
        return scopes.asSequence().map { it[symbol] }.firstOrNull { it != null }
    }

    internal fun getFree(symbol: Symbol): Int? {

        if (parent == null) return null

        return when (val local = parent.getLocal(symbol)) {
            null -> {
                val free = parent.getFree(symbol) ?: return null
                storeFreeValue(free, false)
            }
            else -> {
                local.isCaptured = true
                storeFreeValue(local.index, true)
            }
        }
    }

    private fun storeFreeValue(index: Int, isLocal: Boolean): Int {

        val cache = freeSymbols.indexOfFirst {
            it.index == index && it.isLocal == isLocal
        }

        if (cache != -1) return cache

        freeSymbols += FreeSymbol(index, isLocal)
        return freeSymbols.size - 1
    }

}

internal class FreeSymbol(val index: Int, val isLocal: Boolean)

internal class Local(val index: Int, var isCaptured: Boolean)
