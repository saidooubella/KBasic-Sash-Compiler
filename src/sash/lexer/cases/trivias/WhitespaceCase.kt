package sash.lexer.cases.trivias

import sash.errors.ErrorsReporter
import sash.input.CharInput
import sash.lexer.cases.CheckResult
import sash.lexer.cases.TokenCase
import sash.lexer.cases.tokens.isNextNotNewLine
import sash.lexer.token.TokenInfo
import sash.lexer.token.TokenType
import sash.span.Span

internal object WhitespaceCase : TokenCase {

    override fun perform(input: CharInput, reporter: ErrorsReporter, extra: Any?): TokenInfo {
        val builder = StringBuilder()
        val start = input.position()
        while (!input.isFinished && input.isWhitespace())
            builder.append(input.consume())
        val end = input.position()
        val span = Span(start, end)
        return TokenInfo(builder.toString(), TokenType.Whitespace, null, span)
    }

    override fun check(input: CharInput): CheckResult {
        return CheckResult(input.isWhitespace())
    }

    private fun CharInput.isWhitespace(): Boolean {
        return isNextNotNewLine() && current.isWhitespace()
    }
}
