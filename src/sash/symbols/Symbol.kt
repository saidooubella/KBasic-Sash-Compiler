package sash.symbols

import sash.types.AbstractType
import sash.types.FunctionType

internal abstract class Symbol {
    internal abstract val name: String
    internal abstract val type: AbstractType
}

internal data class VariableSymbol(
    override val name: String,
    override val type: AbstractType,
    internal val readOnly: Boolean
) : Symbol()

internal data class ParameterSymbol(
    override val name: String,
    override val type: AbstractType
) : Symbol()

internal data class FunctionSymbol(
    override val name: String,
    override val type: FunctionType,
    internal val parameters: List<ParameterSymbol>
) : Symbol()
