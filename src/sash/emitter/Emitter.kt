@file:Suppress("NOTHING_TO_INLINE")

package sash.emitter

import sash.binder.nodes.*
import sash.emitter.consts.ConstantPool
import sash.emitter.consts.FunctionPtr
import sash.emitter.scopes.GlobalScope
import sash.emitter.scopes.LocalScope
import sash.emitter.scopes.LoopScope
import sash.emitter.tools.BufferArray
import sash.symbols.Symbol
import sash.types.*
import java.io.OutputStream

internal class Emitter(private val program: ProgramBindNode) {

    private var localScope = LocalScope()
    private val globalScope = GlobalScope()
    private val loopScope = LoopScope()

    private val constsPool = ConstantPool()
    private val bytecode = BufferArray()

    internal fun writeTo(output: OutputStream) {

        program.statements.forEach { it.emit() }
        bytecode.appendByte(Instructions.HALT)

        constsPool.writeTo(output)
        BufferArray(2).apply {
            appendUShort(globalScope.size)
            writeTo(output)
        }

        bytecode.writeTo(output)
    }

    private inline fun emitInstantlyJump(opcode: Byte, offset: Int) {
        bytecode.setShort(emitPlaceholderJump(opcode), offset - bytecode.size)
    }

    private inline fun emitPlaceholderJump(opcode: Byte): Int {
        bytecode.appendByte(opcode)
        return bytecode.appendShort(Short.MAX_VALUE.toInt())
    }

    private inline fun patchJump(index: Int, target: Int = bytecode.size) {
        bytecode.setShort(index, target - (index + 2))
    }

    private fun StatementBindNode.emit(): Any? {
        return when (this) {
            is FunctionStatementBindNode -> {

                val skip = emitPlaceholderJump(Instructions.GOTO)
                val start = bytecode.size

                if (localScope.isGlobal()) {
                    globalScope.putGlobal(function)
                } else {
                    localScope.putLocal(function)
                }

                localScope = LocalScope(localScope)
                localScope.startScope()

                function.parameters.forEach(localScope::putLocal)

                block.emit()

                if (shouldInsertReturn) {
                    // TODO: Change the return value with a value of the type `sash.Unit`.
                    bytecode.appendByte(Instructions.PUSH_TRUE)
                    bytecode.appendByte(Instructions.RETURN)
                }

                localScope.endScope()
                val freeSymbols = localScope.freeSymbols()
                localScope = requireNotNull(localScope.parent)

                patchJump(skip)

                bytecode.appendByte(Instructions.CONSTANT)
                bytecode.appendUShort(constsPool.addConstant(FunctionPtr(function.parameters.size, start)))

                if (freeSymbols.isNotEmpty()) {
                    bytecode.appendByte(Instructions.CLOSURE)
                    bytecode.appendUShort(freeSymbols.size)
                    freeSymbols.forEach {
                        bytecode.appendByte(if (it.isLocal) 1 else 0)
                        bytecode.appendUShort(it.index)
                    }
                }

                if (localScope.isGlobal()) {
                    bytecode.appendByte(Instructions.SET_GLOBAL)
                    bytecode.appendUShort(requireNotNull(globalScope.getGlobal(function)))
                    bytecode.appendByte(Instructions.POP)
                } else Unit
            }
            is ReturnStatementBindNode -> {
                // TODO: Change the return value with a value of the type `sash.Unit`.
                value?.emit() ?: bytecode.appendByte(Instructions.PUSH_TRUE)
                bytecode.appendByte(Instructions.RETURN)
            }
            is VariableStatementBindNode -> {
                value.emit()
                if (localScope.isGlobal()) {
                    bytecode.appendByte(Instructions.SET_GLOBAL)
                    bytecode.appendUShort(globalScope.putGlobal(variable))
                    bytecode.appendByte(Instructions.POP)
                } else {
                    localScope.putLocal(variable)
                }
            }
            is ContinueStatementBindNode -> {
                val index = emitPlaceholderJump(Instructions.GOTO)
                loopScope.addContinueIndex(index)
            }
            is BreakStatementBindNode -> {
                val index = emitPlaceholderJump(Instructions.GOTO)
                loopScope.addBreakIndex(index)
            }
            is IfStatementBindNode -> {
                condition.emit()
                val ifIndex = emitPlaceholderJump(Instructions.GOTO_DROP_FALSE)
                ifBlock.emit()
                if (elseBlock == null) {
                    patchJump(ifIndex)
                } else {
                    val endIndex = emitPlaceholderJump(Instructions.GOTO)
                    patchJump(ifIndex)
                    elseBlock.emit()
                    patchJump(endIndex)
                }
            }
            is WhileStatementBindNode -> {
                loopScope.startScope()
                val continueIndex = bytecode.size
                condition.emit()
                val end = emitPlaceholderJump(Instructions.GOTO_DROP_FALSE)
                block.emit()
                emitInstantlyJump(Instructions.GOTO, continueIndex)
                patchJump(end)
                loopScope.endScope(
                    continueBlock = { patchJump(it, continueIndex) },
                    breakBlock = { patchJump(it) }
                )
            }
            is DoWhileStatementBindNode -> {
                loopScope.startScope()
                val start = bytecode.size
                block.emit()
                val continueIndex = bytecode.size
                condition.emit()
                emitInstantlyJump(Instructions.GOTO_DROP_TRUE, start)
                loopScope.endScope(
                    continueBlock = { patchJump(it, continueIndex) },
                    breakBlock = { patchJump(it) }
                )
            }
            is ExpressionStatementBindNode -> {
                expression.emit()
                bytecode.appendByte(Instructions.POP)
            }
            is BlockStatementBindNode -> {
                localScope.startScope()
                statements.forEach { it.emit() }
                localScope.currentLocals().forEach {
                    val opcode = when (it.isCaptured) {
                        true -> Instructions.CLOSE_FREE
                        else -> Instructions.POP
                    }
                    bytecode.appendByte(opcode)
                }
                localScope.endScope()
            }
            is PrintStatementBindNode -> {
                expression.emit()
                bytecode.appendByte(Instructions.PRINT)
            }
        }
    }

    private sealed class OpcodeMode(val globalOpcode: Byte, val freeOpcode: Byte, val localOpcode: Byte) {
        internal object GET : OpcodeMode(Instructions.GET_GLOBAL, Instructions.GET_FREE, Instructions.GET_LOCAL)
        internal object SET : OpcodeMode(Instructions.SET_GLOBAL, Instructions.SET_FREE, Instructions.SET_LOCAL)
    }

    private class SymbolInfo(val opcode: Byte, val index: Int)

    private fun getSymbolIndex(symbol: Symbol, mode: OpcodeMode): SymbolInfo {

        var index = localScope.getLocal(symbol)?.index
        if (index != null) return SymbolInfo(mode.localOpcode, index)

        index = localScope.getFree(symbol)
        if (index != null) return SymbolInfo(mode.freeOpcode, index)

        index = globalScope.getGlobal(symbol)
        if (index != null) return SymbolInfo(mode.globalOpcode, index)

        error("Unknown symbol `${symbol.name}`.")
    }

    private fun ExpressionBindNode.emit(): Any? {
        return when (this) {
            is ErrorExpressionBindNode -> error("Internal error")
            is CallExpressionBindNode -> {
                arguments.forEach { it.emit() }
                target.emit()
                bytecode.appendByte(Instructions.CALL)
            }
            is VariableExpressionBindNode -> {
                val symbolInfo = getSymbolIndex(symbol, OpcodeMode.GET)
                bytecode.appendByte(symbolInfo.opcode)
                bytecode.appendUShort(symbolInfo.index)
            }
            is TernaryExpressionBindNode -> {
                condition.emit()
                val ifIndex = emitPlaceholderJump(Instructions.GOTO_DROP_FALSE)
                ifExpression.emit()
                val endIndex = emitPlaceholderJump(Instructions.GOTO)
                patchJump(ifIndex)
                elseExpression.emit()
                patchJump(endIndex)
            }
            is BinaryOperationExpressionBindNode -> {
                when (operation) {
                    BinaryOperation.Addition -> {
                        left.emit()
                        right.emit()
                        bytecode.appendByte(Instructions.ADD)
                    }
                    BinaryOperation.Subtraction -> {
                        left.emit()
                        right.emit()
                        bytecode.appendByte(Instructions.SUB)
                    }
                    BinaryOperation.Multiplication -> {
                        left.emit()
                        right.emit()
                        bytecode.appendByte(Instructions.MUL)
                    }
                    BinaryOperation.Division -> {
                        left.emit()
                        right.emit()
                        bytecode.appendByte(Instructions.DIV)
                    }
                    BinaryOperation.Equals -> {
                        left.emit()
                        right.emit()
                        bytecode.appendByte(Instructions.EQUALS)
                    }
                    BinaryOperation.NotEquals -> {
                        left.emit()
                        right.emit()
                        bytecode.appendByte(Instructions.EQUALS)
                        bytecode.appendByte(Instructions.NOT)
                    }
                    BinaryOperation.GreaterThanEqual -> {
                        left.emit()
                        right.emit()
                        bytecode.appendByte(Instructions.LESS)
                        bytecode.appendByte(Instructions.NOT)
                    }
                    BinaryOperation.LessThanEqual -> {
                        left.emit()
                        right.emit()
                        bytecode.appendByte(Instructions.GREATER)
                        bytecode.appendByte(Instructions.NOT)
                    }
                    BinaryOperation.GreaterThan -> {
                        left.emit()
                        right.emit()
                        bytecode.appendByte(Instructions.GREATER)
                    }
                    BinaryOperation.LessThan -> {
                        left.emit()
                        right.emit()
                        bytecode.appendByte(Instructions.LESS)
                    }
                    BinaryOperation.LogicalAnd -> {
                        emitShortCircuitExpr(BinaryOperation.LogicalAnd, Instructions.GOTO_FALSE_OR_DROP)
                    }
                    BinaryOperation.LogicalOr -> {
                        emitShortCircuitExpr(BinaryOperation.LogicalOr, Instructions.GOTO_TRUE_OR_DROP)
                    }
                    BinaryOperation.Concat -> {
                        val flattenExpr = flattenBinaryExpr(BinaryOperation.Concat)
                        flattenExpr.reversed().forEach { expr -> expr.emit() }
                        bytecode.appendByte(Instructions.CONCAT)
                        bytecode.appendUShort(flattenExpr.size)
                    }
                }
            }
            is UnaryOperationExpressionBindNode -> {
                operand.emit()
                when (operation) {
                    UnaryOperation.Identity -> Unit // Do Nothing
                    UnaryOperation.LogicalNegation -> {
                        bytecode.appendByte(Instructions.NOT)
                    }
                    UnaryOperation.Negation -> {
                        bytecode.appendByte(Instructions.NEG)
                    }
                }
            }
            is ParenthesisedExpressionBindNode -> {
                expression.emit()
            }
            is LiteralExpressionBindNode -> {
                when (type) {
                    ErrorType, UnitType, AnyType -> error("Internal error")
                    CharType -> TODO("Add support for character type")
                    BooleanType -> {
                        val opcode = when (value as Boolean) {
                            true -> Instructions.PUSH_TRUE
                            else -> Instructions.PUSH_FALSE
                        }
                        bytecode.appendByte(opcode)
                    }
                    DoubleType, FloatType, LongType, IntType, StringType -> {
                        bytecode.appendByte(Instructions.CONSTANT)
                        bytecode.appendUShort(constsPool.addConstant(value))
                    }
                }
            }
            is AssignmentExpressionBindNode -> {
                value.emit()
                val symbolInfo = getSymbolIndex(variable, OpcodeMode.SET)
                bytecode.appendByte(symbolInfo.opcode)
                bytecode.appendUShort(symbolInfo.index)
            }
        }
    }

    private fun BinaryOperationExpressionBindNode.emitShortCircuitExpr(
        operation: BinaryOperation,
        jumpOpcode: Byte
    ) {
        val flattenExpr = flattenBinaryExpr(operation)
        val patch = IntArray(flattenExpr.size - 1)
        flattenExpr.forEachIndexed { index, expression ->
            expression.emit()
            if (index < flattenExpr.lastIndex) {
                bytecode.appendByte(jumpOpcode)
                val skip = bytecode.appendUShort(-1)
                patch[index] = skip
            }
        }
        patch.forEach { bytecode.setUShort(it, bytecode.size) }
    }
}

private fun BinaryOperationExpressionBindNode.flattenBinaryExpr(
    operation: BinaryOperation
): List<ExpressionBindNode> {
    if (this.operation != operation) return emptyList()
    return mutableListOf<ExpressionBindNode>().apply {
        left.flattenBinaryExpr(this, operation)
        right.flattenBinaryExpr(this, operation)
    }
}

private fun ExpressionBindNode.flattenBinaryExpr(
    list: MutableList<ExpressionBindNode>,
    operation: BinaryOperation
) {
    if (this is BinaryOperationExpressionBindNode && this.operation == operation) {
        left.flattenBinaryExpr(list, operation)
        right.flattenBinaryExpr(list, operation)
    } else list.add(this)
}
