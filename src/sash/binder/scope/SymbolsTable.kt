package sash.binder.scope

import sash.symbols.Symbol
import sash.types.AbstractType

internal class SymbolsTable private constructor(private var scope: SymbolsScope) {

    internal fun pushScope() {
        scope = SymbolsScope.wrap(scope)
    }

    internal fun popScope() {
        scope = scope.apply { clear() }.run { requireParent() }
    }

    internal fun putSymbol(symbol: Symbol) = scope.putSymbol(symbol)

    internal fun getSymbol(name: String): Symbol? = scope.get { getSymbol(name) }

    internal fun getType(name: String): AbstractType? = scope.get { getType(name) }

    internal fun hasSymbol(name: String): Boolean = scope.hasSymbol(name)

    companion object {
        internal fun create(): SymbolsTable = SymbolsTable(SymbolsScope.create())
    }
}

private inline fun <V : Any> SymbolsScope.get(action: SymbolsScope.() -> V?): V? {
    var scope: SymbolsScope? = this
    while (scope != null) {
        scope.action()?.let { return it }
        scope = scope.parent
    }
    return null
}

internal inline fun <R> SymbolsTable.scoped(action: () -> R): R {
    try {
        pushScope()
        return action()
    } finally {
        popScope()
    }
}
