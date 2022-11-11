package sash.lexer.cases.tokens

import sash.errors.ErrorsReporter
import sash.input.CharInput
import sash.lexer.cases.CheckResult
import sash.lexer.cases.TokenCase
import sash.lexer.token.TokenInfo
import sash.lexer.token.TokenType
import sash.span.Span

internal object IdentifierCase : TokenCase {

    override fun perform(input: CharInput, reporter: ErrorsReporter, extra: Any?): TokenInfo {

        val builder = StringBuilder()
        val start = input.position()

        while (!input.isFinished && input.current.isIdentifierFull()) {
            builder.append(input.consume())
        }

        val end = input.position()
        val span = Span(start, end)
        val identifier = builder.toString()

        return TokenInfo(identifier, identifier.tokenType(), null, span)
    }

    override fun check(input: CharInput): CheckResult {
        return CheckResult(input.current.isIdentifier())
    }

    private val KEYWORDS: Map<String, TokenType> = mapOf(
        "continue" to TokenType.ContinueKeyword,
        "return" to TokenType.ReturnKeyword,
        "break" to TokenType.BreakKeyword,
        "print" to TokenType.PrintKeyword,
        "false" to TokenType.FalseKeyword,
        "while" to TokenType.WhileKeyword,
        "else" to TokenType.ElseKeyword,
        "true" to TokenType.TrueKeyword,
        "def" to TokenType.DefKeyword,
        "fun" to TokenType.FunKeyword,
        "let" to TokenType.LetKeyword,
        "do" to TokenType.DoKeyword,
        "if" to TokenType.IfKeyword
    )

    private fun String.tokenType() = KEYWORDS[this] ?: TokenType.Identifier

    private fun Char.isIdentifier() = isLetter() || this == '_' || this == '$'

    private fun Char.isIdentifierFull() = isIdentifier() || isDigit()
}