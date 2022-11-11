package sash.lexer.cases.tokens

import sash.errors.ErrorsReporter
import sash.input.CharInput
import sash.lexer.cases.CheckResult
import sash.lexer.cases.TokenCase
import sash.lexer.token.TokenInfo
import sash.lexer.token.TokenType
import sash.span.Span

internal object StringCase : TokenCase {

    override fun perform(input: CharInput, reporter: ErrorsReporter, extra: Any?): TokenInfo {

        val textBuilder = StringBuilder()
        val strBuilder = StringBuilder()

        val start = input.position()
        textBuilder.append(input.consume())

        var isEnded = false

        while (!input.isFinished && input.isNextNotNewLine()) {

            if (input.current == '\\') {
                input.escapeChar(reporter, textBuilder)
                    ?.also { strBuilder.append(it) }
                continue
            }

            val current = input.consume()

            if (current == '"') {
                textBuilder.append(current)
                isEnded = true
                break
            }

            textBuilder.append(current)
            strBuilder.append(current)
        }

        val end = input.position()
        val span = Span(start, end)
        val text = textBuilder.toString()
        val string = strBuilder.toString()

        if (!isEnded) reporter.reportUnterminatedStringLiteral(Span(span.end, span.end))

        return TokenInfo(text, TokenType.String, string, span)
    }

    override fun check(input: CharInput): CheckResult {
        return CheckResult(input.current == '"')
    }
}