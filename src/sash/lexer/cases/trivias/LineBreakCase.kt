package sash.lexer.cases.trivias

import sash.errors.ErrorsReporter
import sash.input.CharInput
import sash.lexer.cases.CheckResult
import sash.lexer.cases.TokenCase
import sash.lexer.token.TokenInfo
import sash.lexer.token.TokenType
import sash.span.Span

internal object LineBreakCase : TokenCase {

    override fun perform(input: CharInput, reporter: ErrorsReporter, extra: Any?): TokenInfo {
        val builder = StringBuilder()
        val start = input.position()
        repeat(extra as Int) { builder.append(input.consume()) }
        val end = input.position()
        val span = Span(start, end)
        return TokenInfo(builder.toString(), TokenType.LineBreak, null, span)
    }

    override fun check(input: CharInput): CheckResult {
        val length = lineBreakLength(input.current, input.peek(1))
        return CheckResult(length > 0, length)
    }

    private fun lineBreakLength(current: Char, next: Char): Int {
        return if (current == '\r' && next == '\n') 2 else if (current == '\n') 1 else 0
    }
}