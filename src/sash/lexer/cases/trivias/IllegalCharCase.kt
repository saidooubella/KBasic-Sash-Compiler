package sash.lexer.cases.trivias

import sash.errors.ErrorsReporter
import sash.input.CharInput
import sash.lexer.cases.CheckResult
import sash.lexer.cases.TokenCase
import sash.lexer.token.TokenInfo
import sash.lexer.token.TokenType
import sash.span.Span

internal object IllegalCharCase : TokenCase {

    override fun perform(input: CharInput, reporter: ErrorsReporter, extra: Any?): TokenInfo {
        val start = input.position()
        val character = input.consume()
        val span = Span(start, input.position())
        reporter.reportIllegalCharacter(span, character)
        return TokenInfo(character.toString(), TokenType.IllegalChar, null, span)
    }

    override fun check(input: CharInput): CheckResult {
        return CheckResult(true)
    }
}
