@file:Suppress("NOTHING_TO_INLINE")

package sash.parser

import sash.errors.ErrorsReporter
import sash.input.Input
import sash.lexer.token.Token
import sash.lexer.token.TokenType
import sash.parser.nodes.*
import sash.span.Spannable
import java.io.Closeable

internal class Parser(
    private val lexer: Input<Token, Token>,
    private val errors: ErrorsReporter
) : Closeable {

    private var errorsFlag = true

    internal fun createProgramNode(): ProgramNode {

        val statements = mutableListOf<StatementNode>()

        while (!lexer.isFinished) avoidLooping {
            val statement = declaration()
            statements += statement
        }

        return ProgramNode(statements)
    }

    private fun declaration(): StatementNode {

        if (match(TokenType.LetKeyword))
            return variableStatement(true)

        if (match(TokenType.DefKeyword))
            return variableStatement(false)

        if (match(TokenType.FunKeyword))
            return functionStatement()

        return statement()
    }

    private fun functionStatement(): StatementNode {
        val keyword = lexer.consume()
        val identifier = consume(TokenType.Identifier, "identifier")
        val open = consume(TokenType.OpenParentheses, "(")
        val params = parameters()
        val close = consume(TokenType.CloseParentheses, ")")
        val type = optionalTypeClause()
        val block = statementsBlock()
        return FunctionStatementNode(keyword, identifier, open, params, close, type, block)
    }

    private fun parameters(): List<ParamClause> {
        val parameters = mutableListOf<ParamClause>()
        while (!lexer.isFinished && !match(TokenType.CloseParentheses)) avoidLooping {
            parameters.add(paramClause())
            if (!match(TokenType.CloseParentheses))
                consume(TokenType.Comma, ",")
        }
        return parameters
    }

    private fun paramClause(): ParamClause {
        val identifier = consume(TokenType.Identifier, "identifier")
        val type = typeClause()
        return ParamClause(identifier, type)
    }

    private fun variableStatement(readOnly: Boolean): VariableStatementNode {
        val keyword = lexer.consume()
        val identifier = consume(TokenType.Identifier, "identifier")
        val type = optionalTypeClause()
        val equal = consume(TokenType.Equal, "=")
        val value = expression()
        return VariableStatementNode(keyword, identifier, type, readOnly, equal, value)
    }

    private fun statement(): StatementNode {

        if (match(TokenType.IfKeyword))
            return ifStatement()

        if (match(TokenType.WhileKeyword))
            return whileStatement()

        if (match(TokenType.DoKeyword))
            return doWhileStatement()

        if (match(TokenType.ContinueKeyword))
            return ContinueStatement(lexer.consume())

        if (match(TokenType.BreakKeyword))
            return BreakStatement(lexer.consume())

        if (match(TokenType.PrintKeyword))
            return PrintStatement(lexer.consume(), expression())

        if (match(TokenType.ReturnKeyword)) {
            val keyword = lexer.consume()
            val value = returnValue(keyword)
            return ReturnStatementNode(keyword, value)
        }

        if (match(TokenType.OpenCurly))
            return statementsBlock()

        return ExpressionStatementNode(expression())
    }

    private fun returnValue(token: Token): ExpressionNode? {
        val isValid = !lexer.isFinished && token atSameLine lexer.current && when (lexer.current.type) {
            TokenType.Plus -> true
            TokenType.Bang -> true
            TokenType.Minus -> true
            TokenType.OpenCurly -> true
            TokenType.OpenParentheses -> true
            TokenType.FunKeyword -> true
            TokenType.TrueKeyword -> true
            TokenType.FalseKeyword -> true
            TokenType.Identifier -> true
            TokenType.ReturnKeyword -> true
            TokenType.BreakKeyword -> true
            TokenType.ContinueKeyword -> true
            TokenType.Long -> true
            TokenType.Float -> true
            TokenType.String -> true
            TokenType.Integer -> true
            TokenType.Double -> true
            TokenType.Character -> true
            // #################################
            TokenType.Whitespace -> false
            TokenType.LineBreak -> false
            TokenType.LineComment -> false
            TokenType.BlockComment -> false
            TokenType.IllegalChar -> false
            TokenType.Slash -> false
            TokenType.Star -> false
            TokenType.CloseParentheses -> false
            TokenType.PipePipe -> false
            TokenType.AmpersandAmpersand -> false
            TokenType.EqualEqual -> false
            TokenType.BangEqual -> false
            TokenType.GreaterThanEqual -> false
            TokenType.LessThanEqual -> false
            TokenType.GreaterThan -> false
            TokenType.LessThan -> false
            TokenType.Equal -> false
            TokenType.Colon -> false
            TokenType.CloseCurly -> false
            TokenType.Comma -> false
            TokenType.Question -> false
            TokenType.LetKeyword -> false
            TokenType.DefKeyword -> false
            TokenType.PrintKeyword -> false
            TokenType.IfKeyword -> false
            TokenType.ElseKeyword -> false
            TokenType.WhileKeyword -> false
            TokenType.DoKeyword -> false
            TokenType.EndOfFile -> false
        }
        return if (isValid) expression() else null
    }

    private fun doWhileStatement(): DoWhileStatementNode {
        val doKeyword = lexer.consume()
        val block = statement()
        val whileKeyword = consume(TokenType.WhileKeyword, "while")
        val open = consume(TokenType.OpenParentheses, "(")
        val condition = expression()
        val close = consume(TokenType.CloseParentheses, ")")
        return DoWhileStatementNode(doKeyword, block, whileKeyword, open, condition, close)
    }

    private fun whileStatement(): WhileStatementNode {
        val keyword = lexer.consume()
        val open = consume(TokenType.OpenParentheses, "(")
        val condition = expression()
        val close = consume(TokenType.CloseParentheses, ")")
        val block = statement()
        return WhileStatementNode(keyword, open, condition, close, block)
    }

    private fun ifStatement(): IfStatementNode {
        val keyword = lexer.consume()
        val open = consume(TokenType.OpenParentheses, "(")
        val condition = expression()
        val close = consume(TokenType.CloseParentheses, ")")
        val block = statement()
        val elseClause = optionalElseClause()
        return IfStatementNode(keyword, open, condition, close, block, elseClause)
    }

    private fun optionalElseClause(): ElseClause? {
        if (!match(TokenType.ElseKeyword)) return null
        val keyword = consume(TokenType.ElseKeyword, "else")
        val block = statement()
        return ElseClause(keyword, block)
    }

    private fun statementsBlock(): BlockStatementNode {
        val open = consume(TokenType.OpenCurly, "{")
        val statements = mutableListOf<StatementNode>()
        while (!lexer.isFinished && !match(TokenType.CloseCurly))
            avoidLooping { statements += declaration() }
        val close = consume(TokenType.CloseCurly, "}")
        return BlockStatementNode(open, statements, close)
    }

    private fun optionalTypeClause(): TypeClause? {
        return if (!match(TokenType.Colon)) null else typeClause()
    }

    private fun typeClause(): TypeClause {
        val colon = lexer.consume()
        val type = valueType()
        return TypeClause(colon, type)
    }

    private fun valueType(): ValueType {
        return ValueType.Normal(consume(TokenType.Identifier, "type"))
    }

    private fun expression(): ExpressionNode {
        return assignment()
    }

    private fun assignment(): ExpressionNode {

        val target = ternary()

        if (match(TokenType.Equal)) {
            val operation = lexer.consume()
            val expression = assignment()
            return AssignmentExpressionNode(target, operation, expression)
        }

        return target
    }

    private fun ternary(): ExpressionNode {

        val condition = disjunction()

        if (match(TokenType.Question)) {
            val question = consume(TokenType.Question, "?")
            val ifExpression = ternary()
            val colon = consume(TokenType.Colon, ":")
            val elseExpression = ternary()
            return TernaryExpressionNode(condition, question, ifExpression, colon, elseExpression)
        }

        return condition
    }

    private fun disjunction(): ExpressionNode {
        return operation(TokenType.PipePipe) { conjunction() }
    }

    private fun conjunction(): ExpressionNode {
        return operation(TokenType.AmpersandAmpersand) { equality() }
    }

    private fun equality(): ExpressionNode {
        return operation(TokenType.EqualEqual, TokenType.BangEqual) { comparison() }
    }

    private fun comparison(): ExpressionNode {
        return operation(
            TokenType.GreaterThan,
            TokenType.GreaterThanEqual,
            TokenType.LessThan,
            TokenType.LessThanEqual
        ) { additive() }
    }

    private fun additive(): ExpressionNode {
        return operation(TokenType.Plus, TokenType.Minus) { multiplicative() }
    }

    private fun multiplicative(): ExpressionNode {
        return operation(TokenType.Star, TokenType.Slash) { unary() }
    }

    private fun unary(): ExpressionNode {

        if (matchAny(TokenType.Plus, TokenType.Minus, TokenType.Bang)) {
            val op = lexer.consume()
            val operand = unary()
            return UnaryOperationExpressionNode(op, operand)
        }

        return postfix()
    }

    private fun postfix(): ExpressionNode {
        var left = primary()
        while (!lexer.isFinished && left atSameLine lexer.current) {
            left = if (match(TokenType.OpenParentheses)) callExpression(left) else break
        }
        return left
    }

    private fun callExpression(left: ExpressionNode): CallExpressionNode {
        val open = lexer.consume()
        val args = arguments()
        val close = consume(TokenType.CloseParentheses, ")")
        return CallExpressionNode(left, open, args, close)
    }

    private fun arguments(): List<ExpressionNode> {
        val arguments = mutableListOf<ExpressionNode>()
        while (!lexer.isFinished && !match(TokenType.CloseParentheses)) avoidLooping {
            arguments.add(expression())
            if (!match(TokenType.CloseParentheses)) consume(TokenType.Comma, ",")
        }
        return arguments
    }

    private fun primary(): ExpressionNode {

        if (
            matchAny(
                TokenType.String, TokenType.Character,
                TokenType.Integer, TokenType.Float,
                TokenType.Long, TokenType.Double
            )
        ) {
            val literal = lexer.consume()
            return LiteralExpressionNode(literal, literal.requireValue(), literal.span)
        }

        if (matchAny(TokenType.TrueKeyword, TokenType.FalseKeyword)) {
            val boolean = lexer.consume()
            val value = boolean.type == TokenType.TrueKeyword
            return LiteralExpressionNode(boolean, value, boolean.span)
        }

        if (match(TokenType.OpenParentheses)) {
            val open = lexer.consume()
            val expr = expression()
            val close = lexer.consume()
            return ParenthesisedExpressionNode(open, expr, close)
        }

        return VariableExpressionNode(consume(TokenType.Identifier, "expression"))
    }

    private fun consume(type: TokenType, expected: String): Token {

        if (match(type)) {
            if (!errorsFlag) errorsFlag = true
            return lexer.consume()
        }

        val token = lexer.current

        if (errorsFlag) {
            errors.reportUnexpectedToken(token, token.text, expected)
            errorsFlag = false
        }

        return Token("", type, null, token.span, listOf(), listOf())
    }

    // private inline fun matchAll(vararg types: TokenType): Boolean {
    //     return !lexer.isFinished && types.allIndexed { index, token -> lexer.peek(index).type == token }
    // }

    private inline fun matchAny(vararg types: TokenType): Boolean {
        return !lexer.isFinished && types.any { lexer.current.type == it }
    }

    private inline fun match(type: TokenType): Boolean {
        return !lexer.isFinished && lexer.current.type == type
    }

    private inline fun operation(vararg types: TokenType, next: () -> ExpressionNode): ExpressionNode {
        var left = next()
        while (!lexer.isFinished && left atSameLine lexer.current && matchAny(*types)) {
            val operation = lexer.consume()
            left = BinaryOperationExpressionNode(left, operation, next())
        }
        return left
    }

    private infix fun Spannable.atSameLine(that: Spannable) =
        this.span.end.line == that.span.start.line

    private inline fun avoidLooping(block: () -> Unit) {
        val current = lexer.current
        block()
        if (current === lexer.current)
            lexer.advance()
        errorsFlag = true
    }

    override fun close() = lexer.close()
}
