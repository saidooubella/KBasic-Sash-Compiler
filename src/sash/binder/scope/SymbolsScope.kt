@file:Suppress("MoveLambdaOutsideParentheses")

package sash.binder.scope

import sash.symbols.Symbol
import sash.tools.putValue
import sash.types.*

internal class SymbolsScope private constructor(internal val parent: SymbolsScope?) {

    private val symbols = HashMap<String, Symbol>()
    private val types = HashMap<String, AbstractType>()

    internal fun putSymbol(symbol: Symbol) = symbols.putValue(symbol.name, symbol)

    internal fun putType(type: AbstractType) = types.putValue(type.name, type)

    internal fun getSymbol(name: String): Symbol? = symbols[name]

    internal fun getType(name: String): AbstractType? = types[name]

    internal fun hasSymbol(name: String): Boolean = name in symbols

    internal fun requireParent(): SymbolsScope = parent ?: throw IllegalStateException("Parent scope not available")

    internal fun clear() {
        symbols.clear()
        types.clear()
    }

    companion object {

        private val GLOBAL_SCOPE = SymbolsScope(null).apply {
            putType(BooleanType)
            putType(DoubleType)
            putType(StringType)
            putType(FloatType)
            putType(LongType)
            putType(AnyType)
            putType(CharType)
            putType(UnitType)
            putType(IntType)
        }

        internal fun create(): SymbolsScope = SymbolsScope(GLOBAL_SCOPE)

        internal fun wrap(parent: SymbolsScope): SymbolsScope = SymbolsScope(parent)
    }
}