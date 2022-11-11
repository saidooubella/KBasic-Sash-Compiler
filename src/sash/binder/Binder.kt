@file:Suppress("NOTHING_TO_INLINE")

package sash.binder

import sash.binder.nodes.*
import sash.binder.scope.SymbolsTable
import sash.binder.scope.scoped
import sash.errors.ErrorsReporter
import sash.parser.nodes.*
import sash.span.Span
import sash.span.plus
import sash.symbols.FunctionSymbol
import sash.symbols.ParameterSymbol
import sash.symbols.VariableSymbol
import sash.tools.forEachReversed
import sash.tools.peekOrNull
import sash.tools.stackOf
import sash.types.*
import kotlin.math.min

internal class Binder(private val program: ProgramNode, private val errors: ErrorsReporter) {

    private val symbolsTable: SymbolsTable = SymbolsTable.create()
    private val scopeTracker: ScopeTracker = ScopeTracker()

    internal fun createProgramNode(): ProgramBindNode {
        val statements = program.statements.map { it.bind() }
        checkReturnPaths(statements, UnitType)
        return ProgramBindNode(statements)
    }

    private fun StatementNode.bind(): StatementBindNode {
        return when (this) {
            is ExpressionStatementNode -> bindExpressionStatement()
            is FunctionStatementNode -> bindFunctionStatement()
            is VariableStatementNode -> bindVariableStatement()
            is DoWhileStatementNode -> bindDoWhileStatement()
            is ReturnStatementNode -> bindReturnExpression()
            is ContinueStatement -> bindContinueStatement()
            is BlockStatementNode -> bindBlockStatement()
            is WhileStatementNode -> bindWhileStatement()
            is BreakStatement -> bindBreakStatement()
            is PrintStatement -> bindPrintStatement()
            is IfStatementNode -> bindIfStatement()
        }
    }

    private fun ReturnStatementNode.bindReturnExpression(): StatementBindNode {
        return ReturnStatementBindNode(value?.bind(BindingContext.Empty), span)
    }

    private fun FunctionStatementNode.bindFunctionStatement(): StatementBindNode {

        val hasSymbol = symbolsTable.hasSymbol(identifier.text)
        if (hasSymbol) errors.reportAlreadyExistentSymbol(identifier.span, identifier.text)

        val params = bindParamClauses(params)

        val returnType = type?.type?.bindSymbolType() ?: UnitType
        val type = FunctionType(params.map { it.type }, returnType)
        val function = FunctionSymbol(identifier.text, type, params)

        if (!hasSymbol && identifier.text.isNotEmpty()) symbolsTable.putSymbol(function)

        val block = scopeTracker.funScope {
            symbolsTable.scoped {
                params.forEach(symbolsTable::putSymbol)
                block.bindBlockStatement()
            }
        }

        val returnStatus = checkReturnPaths(block, returnType)
        if (!returnStatus.ok()) errors.reportRequireReturnValue(close.span, returnType)

        return FunctionStatementBindNode(function, block, returnStatus.shouldInsert(), span)
    }

    private fun bindParamClauses(paramClauses: List<ParamClause>): List<ParameterSymbol> {
        val parameters = mutableListOf<ParameterSymbol>()
        val paramsLookup = hashSetOf<String>()
        paramClauses.forEach { paramClause ->
            val identifier = paramClause.identifier.text
            if (identifier.isEmpty()) return@forEach
            val type = paramClause.type.type.bindSymbolType()
            if (!paramsLookup.add(identifier))
                errors.reportAlreadyUsedParamName(paramClause.span, identifier)
            parameters.add(ParameterSymbol(identifier, type))
        }
        return parameters
    }

    private fun PrintStatement.bindPrintStatement(): StatementBindNode {
        return PrintStatementBindNode(expression.bind(BindingContext.Empty), span)
    }

    private fun BreakStatement.bindBreakStatement(): StatementBindNode {

        when (scopeTracker.inLoopState()) {
            LoopState.InFunction -> errors.reportJumpThroughFunction(span, "break")
            LoopState.Invalid -> errors.reportOutOfLoopScope(span, "break")
        }

        return BreakStatementBindNode(span)
    }

    private fun ContinueStatement.bindContinueStatement(): StatementBindNode {

        when (scopeTracker.inLoopState()) {
            LoopState.InFunction -> errors.reportJumpThroughFunction(span, "continue")
            LoopState.Invalid -> errors.reportOutOfLoopScope(span, "continue")
        }

        return ContinueStatementBindNode(span)
    }

    private fun DoWhileStatementNode.bindDoWhileStatement(): StatementBindNode {

        val condition = condition.bind(BindingContext(BooleanType))
        val block = scopeTracker.loopScope { block.bind() }

        if (!condition.isError() && condition.type != BooleanType)
            errors.reportInvalidConditionType(condition.span)

        return DoWhileStatementBindNode(block, condition, span)
    }

    private fun WhileStatementNode.bindWhileStatement(): StatementBindNode {

        val condition = condition.bind(BindingContext(BooleanType))
        val block = scopeTracker.loopScope { block.bind() }

        if (!condition.isError() && condition.type != BooleanType)
            errors.reportInvalidConditionType(condition.span)

        return WhileStatementBindNode(condition, block, span)
    }

    private fun IfStatementNode.bindIfStatement(): StatementBindNode {

        val condition = condition.bind(BindingContext(BooleanType))
        val ifBlock = block.bind()
        val elseBlock = elseClause?.block?.bind()

        if (!condition.isError() && condition.type != BooleanType)
            errors.reportInvalidConditionType(condition.span)

        return IfStatementBindNode(condition, ifBlock, elseBlock, span)
    }

    private fun BlockStatementNode.bindBlockStatement(): BlockStatementBindNode {
        symbolsTable.scoped {
            val statements = statements.map { it.bind() }
            return BlockStatementBindNode(statements, span)
        }
    }

    private fun VariableStatementNode.bindVariableStatement(): VariableStatementBindNode {

        val hasSymbol = symbolsTable.hasSymbol(identifier.text)

        if (hasSymbol) errors.reportAlreadyExistentSymbol(identifier.span, identifier.text)

        val explicitType = type?.type?.bindSymbolType()
        val value = value.bind(explicitType?.let { BindingContext(it) } ?: BindingContext.Empty)
        val type = explicitType ?: value.type
        val variable = VariableSymbol(identifier.text, type, readOnly)

        if (!hasSymbol && identifier.text.isNotEmpty()) symbolsTable.putSymbol(variable)

        if (!value.type.assignableTo(type))
            errors.reportWrongAssignment(value.span, value.type, type)

        return VariableStatementBindNode(variable, value, span)
    }

    private fun ExpressionStatementNode.bindExpressionStatement(): StatementBindNode {
        val expression = expression.bind(BindingContext.Empty)
        // if (!expression.isValidStatement)
        //     errors.reportIllegalExpressionStatement(span)
        return ExpressionStatementBindNode(expression)
    }

    private fun ExpressionNode.bind(context: BindingContext): ExpressionBindNode {
        return when (this) {
            is BinaryOperationExpressionNode -> bindBinaryOperationExpression(context)
            is UnaryOperationExpressionNode -> bindUnaryOperationExpression(context)
            is ParenthesisedExpressionNode -> bindParenthesisedExpression(context)
            is AssignmentExpressionNode -> bindAssignmentExpression(context)
            is VariableExpressionNode -> bindVariableExpression()
            is LiteralExpressionNode -> bindLiteralExpression()
            is CallExpressionNode -> bindCallExpression(context)
            is TernaryExpressionNode -> bindTernaryExpression(context)
        }
    }

    private fun CallExpressionNode.bindCallExpression(context: BindingContext): ExpressionBindNode {

        val target = target.bind(context)
        val targetType = target.type

        if (targetType is FunctionType) {
            val (params, returnType) = targetType
            val arguments = validateArguments(context, params, args, open + close)
            return CallExpressionBindNode(target, arguments, returnType, span)
        }

        args.forEach { it.bind(context.copy(expectedType = null)) }
        if (!target.isError()) errors.reportInvalidInvokableTarget(target.span)

        return ErrorExpressionBindNode(span)
    }

    private fun validateArguments(
        context: BindingContext,
        paramsTypes: List<AbstractType>,
        args: List<ExpressionNode>,
        span: Span
    ): List<ExpressionBindNode> {

        val arguments = args.mapIndexed { index, item ->
            item.bind(context.copy(expectedType = paramsTypes.getOrNull(index)))
        }

        if (paramsTypes.size != arguments.size)
            errors.reportUnexpectedArgsSize(span, paramsTypes.size, arguments.size)

        repeat(min(arguments.size, paramsTypes.size)) { index ->
            val argument = arguments[index]
            val paramType = paramsTypes[index]
            if (!argument.type.assignableTo(paramType)) {
                errors.reportInvalidArgumentType(argument.span, paramType, argument.type)
            }
        }

        return arguments
    }

    private fun TernaryExpressionNode.bindTernaryExpression(context: BindingContext): ExpressionBindNode {

        val condition = condition.bind(context)
        val ifExpression = ifExpression.bind(context)
        val elseExpression = elseExpression.bind(context)

        if (!condition.isError() && condition.type != BooleanType)
            errors.reportInvalidConditionType(condition.span)

        val ifExprType = ifExpression.type
        val elseExprType = elseExpression.type

        val type = when (ifExprType) {
            elseExprType -> ifExprType
            else -> AnyType
        }

        return TernaryExpressionBindNode(condition, ifExpression, elseExpression, type, span)
    }

    private fun VariableExpressionNode.bindVariableExpression(): ExpressionBindNode {

        if (identifier.text.isEmpty()) return ErrorExpressionBindNode(span)

        val symbol = symbolsTable.getSymbol(identifier.text) ?: run {
            errors.reportUnknownSymbol(span, identifier.text)
            return ErrorExpressionBindNode(span)
        }

        return VariableExpressionBindNode(symbol, span)
    }

    private fun AssignmentExpressionNode.bindAssignmentExpression(context: BindingContext): ExpressionBindNode {

        val target = target.bind(context)

        if (target is VariableExpressionBindNode && target.symbol is VariableSymbol) {
            val (name, type, readOnly) = target.symbol
            if (readOnly) errors.reportReadOnlyVariableAssignment(target.span, name)
            val value = validateAssignmentValue(context, expression, type)
            return AssignmentExpressionBindNode(target.symbol, value, type, span)
        }

        expression.bind(context.copy(expectedType = null))

        if (!target.isError())
            errors.reportInvalidAssignmentTarget(target.span)

        return ErrorExpressionBindNode(span)
    }

    private fun validateAssignmentValue(
        context: BindingContext,
        expression: ExpressionNode,
        type: AbstractType
    ): ExpressionBindNode {
        return expression.bind(context.copy(expectedType = type)).also { value ->
            if (!value.type.assignableTo(type))
                errors.reportWrongAssignment(value.span, value.type, type)
        }
    }

    private fun ParenthesisedExpressionNode.bindParenthesisedExpression(context: BindingContext): ExpressionBindNode {
        return ParenthesisedExpressionBindNode(expression.bind(context), span)
    }

    private fun BinaryOperationExpressionNode.bindBinaryOperationExpression(context: BindingContext): ExpressionBindNode {

        val left = left.bind(context).nullIfError()
        val right = right.bind(context).nullIfError()

        left ?: return ErrorExpressionBindNode(span)
        right ?: return ErrorExpressionBindNode(span)

        val binder = BinaryOperationBinder(left.type, operation.type, right.type) ?: run {
            errors.reportInvalidBinaryOperation(operation.span, operation.text, left.type, right.type)
            return ErrorExpressionBindNode(span)
        }

        return BinaryOperationExpressionBindNode(left, binder.operation, right, binder.operationType, span)
    }

    private fun UnaryOperationExpressionNode.bindUnaryOperationExpression(context: BindingContext): ExpressionBindNode {
        val value = operand.bind(context).nullIfError() ?: return ErrorExpressionBindNode(span)
        val binder = UnaryOperationBinder(operation.type, value.type) ?: run {
            errors.reportInvalidUnaryOperation(operation.span, operation.text, value.type)
            return ErrorExpressionBindNode(span)
        }
        return UnaryOperationExpressionBindNode(binder.operation, value, binder.operationType, span)
    }

    private fun LiteralExpressionNode.bindLiteralExpression(): ExpressionBindNode {
        return LiteralExpressionBindNode(value, value.bindLiteralType(), span)
    }

    private fun ValueType.bindSymbolType(): AbstractType {
        return when (this) {
            is ValueType.Normal -> when (type.text.isNotEmpty()) {
                true -> symbolsTable.getType(type.text)
                    ?: ErrorType.also { errors.reportUndefinedType(span, type.text) }
                else -> ErrorType
            }
        }
    }

    private fun checkReturnPaths(root: StatementBindNode, type: AbstractType): ReturnStatus {
        val returnStatus = checkReturnPathsInternal(root, type)
        if (returnStatus.invalid() && (type == ErrorType || type == UnitType))
            return ReturnStatus.MustBeInserted
        return returnStatus
    }

    private fun checkReturnPaths(statements: List<StatementBindNode>, type: AbstractType): ReturnStatus {
        val returnStatus = checkReturnPathsInternal(statements, type)
        if (returnStatus.invalid() && (type == ErrorType || type == UnitType))
            return ReturnStatus.MustBeInserted
        return returnStatus
    }

    private fun checkReturnPathsInternal(node: StatementBindNode, type: AbstractType): ReturnStatus {

        when (node) {
            is DoWhileStatementBindNode -> checkReturnPathsInternal(node.block, type)
            is BlockStatementBindNode -> return checkReturnPathsInternal(node.statements, type)
            is WhileStatementBindNode -> checkReturnPathsInternal(node.block, type)
            is BreakStatementBindNode, is ContinueStatementBindNode -> return ReturnStatus.Valid
            is ReturnStatementBindNode -> {
                when (val value = node.value) {
                    null -> if (type != UnitType && type != ErrorType)
                        errors.reportMissingReturnValue(node.span, type)
                    else -> if (!value.type.assignableTo(type))
                        errors.reportWrongReturnValueType(node.span, type, value.type)
                }
                return ReturnStatus.Valid
            }
            is IfStatementBindNode -> {
                val ifResult = checkReturnPathsInternal(node.ifBlock, type)
                val elseResult = node.elseBlock?.let { checkReturnPathsInternal(it, type) }
                    ?: ReturnStatus.MustBeSpecified
                return if (ifResult.valid() && elseResult.valid()) ReturnStatus.Valid else ReturnStatus.MustBeSpecified
            }
        }

        return ReturnStatus.MustBeSpecified
    }

    private fun checkReturnPathsInternal(statements: List<StatementBindNode>, type: AbstractType): ReturnStatus {
        var result = false
        val size = statements.size
        var i = 0
        while (i < size) {
            val statement = statements[i]
            result = result || checkReturnPathsInternal(statement, type).valid()
            if (result && i < size - 1) break
            i++
        }
        for (index in i + 1 until size) {
            val statement = statements[index]
            checkReturnPathsInternal(statement, type)
            errors.reportUnreachedStatement(statement.span)
        }
        return if (result) ReturnStatus.Valid else ReturnStatus.MustBeSpecified
    }

    private sealed class ReturnStatus {
        object MustBeSpecified : ReturnStatus()
        object MustBeInserted : ReturnStatus()
        object Valid : ReturnStatus()
    }

    private inline fun ReturnStatus.shouldInsert() = this == ReturnStatus.MustBeInserted
    private inline fun ReturnStatus.invalid() = this == ReturnStatus.MustBeSpecified
    private inline fun ReturnStatus.valid() = this == ReturnStatus.Valid
    private inline fun ReturnStatus.ok() = this == ReturnStatus.Valid || this == ReturnStatus.MustBeInserted
}

private inline fun ExpressionBindNode.nullIfError() = if (isError()) null else this

private fun Any.bindLiteralType(): PrimitiveType {
    return when (this) {
        is Boolean -> BooleanType
        is Double -> DoubleType
        is String -> StringType
        is Float -> FloatType
        is Char -> CharType
        is Long -> LongType
        is Int -> IntType
        else -> throw IllegalStateException()
    }
}

private data class BindingContext(
    internal val expectedType: AbstractType? = null
) {
    companion object {
        internal val Empty = BindingContext()
    }
}

private sealed class LoopState {
    internal object InFunction : LoopState()
    internal object Invalid : LoopState()
    internal object Valid : LoopState()
}

private class ScopeTracker {

    internal val scopes = stackOf<Scope>()

    internal inline fun <R> loopScope(block: () -> R): R = try {
        scopes.push(Scope.Loop)
        block()
    } finally {
        scopes.pop()
    }

    internal inline fun <R> funScope(block: () -> R): R = try {
        scopes.push(Scope.Function)
        block()
    } finally {
        scopes.pop()
    }

    internal fun inLoopState(): LoopState {
        if (inScope(Scope.Loop)) {
            when (scopes.peekOrNull()) {
                Scope.Function -> return LoopState.InFunction
                Scope.Loop -> return LoopState.Valid
            }
        }
        return LoopState.Invalid
    }

    private fun inScope(scope: Scope): Boolean {
        scopes.forEachReversed {
            if (it == scope) return true
        }
        return false
    }

    private sealed class Scope {
        internal object Function : Scope()
        internal object Loop : Scope()
    }
}
