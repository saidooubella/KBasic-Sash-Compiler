package sash.lexer.cases.tokens

import sash.errors.ErrorsReporter
import sash.input.CharInput
import sash.lexer.cases.CheckResult
import sash.lexer.cases.TokenCase
import sash.lexer.token.TokenInfo
import sash.lexer.token.TokenType
import sash.span.Span

internal object EndOfFileCase : TokenCase {

    override fun perform(input: CharInput, reporter: ErrorsReporter, extra: Any?): TokenInfo {
        val position = input.position()
        val span = Span(position, position)
        return TokenInfo("End of file", TokenType.EndOfFile, null, span)
    }

    override fun check(input: CharInput): CheckResult {
        return CheckResult(input.isFinished)
    }
}