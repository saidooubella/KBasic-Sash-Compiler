package sash.lexer.cases.trivias

import sash.errors.ErrorsReporter
import sash.input.CharInput
import sash.lexer.cases.CheckResult
import sash.lexer.cases.TokenCase
import sash.lexer.token.TokenInfo
import sash.lexer.token.TokenType
import sash.span.Span

internal object BlockCommentCase : TokenCase {

    override fun perform(input: CharInput, reporter: ErrorsReporter, extra: Any?): TokenInfo {

        val builder = StringBuilder()
        val start = input.position()
        var prev1 = '\u0000'
        var prev2 = '\u0000'
        var isEnded: Boolean

        while (run { isEnded = prev1 == '*' && prev2 == '/'; !input.isFinished && !isEnded }) {
            prev1 = prev2
            prev2 = input.consume()
            builder.append(prev2)
        }

        val end = input.position()
        val span = Span(start, end)

        if (!isEnded) reporter.reportUnterminatedBlockComment(Span(span.end, span.end))

        return TokenInfo(builder.toString(), TokenType.BlockComment, null, span)
    }

    override fun check(input: CharInput): CheckResult {
        return CheckResult(input.current == '/' && input.peek(1) == '*')
    }
}