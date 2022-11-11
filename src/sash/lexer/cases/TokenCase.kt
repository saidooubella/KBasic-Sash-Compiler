package sash.lexer.cases

import sash.errors.ErrorsReporter
import sash.input.CharInput
import sash.lexer.token.TokenInfo

internal interface TokenCase {
    fun perform(input: CharInput, reporter: ErrorsReporter, extra: Any?): TokenInfo
    fun check(input: CharInput): CheckResult
}
