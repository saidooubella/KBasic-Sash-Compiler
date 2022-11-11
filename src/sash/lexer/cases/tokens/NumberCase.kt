package sash.lexer.cases.tokens

import sash.errors.ErrorsReporter
import sash.input.CharInput
import sash.lexer.cases.CheckResult
import sash.lexer.cases.TokenCase
import sash.lexer.token.TokenInfo
import sash.lexer.token.TokenType
import sash.span.Span

internal object NumberCase : TokenCase {

    override fun perform(input: CharInput, reporter: ErrorsReporter, extra: Any?): TokenInfo {

        val textBuilder = StringBuilder()
        val numBuilder = StringBuilder()

        val start = input.position()
        var type: TokenType = TokenType.Integer

        while (!input.isFinished && isDecimalNumber(input.current)) {

            val current = input.current

            if (current == 'l' || current == 'L') {
                if (type === TokenType.Integer) {
                    textBuilder.append(input.consume())
                    type = TokenType.Long
                }
                break
            } else if (current == 'f' || current == 'F') {
                textBuilder.append(input.consume())
                type = TokenType.Float
                break
            } else if (current == '.') {
                type = if (isNumber(input.peek(1))) TokenType.Double else break
            }

            numBuilder.append(input.consume())
            textBuilder.append(current)
        }

        val end = input.position()
        val span = Span(start, end)
        val text = textBuilder.toString()
        val number = numBuilder.toString()

        return TokenInfo(text, type, number.parseNumber(reporter, span, type), span)
    }

    override fun check(input: CharInput): CheckResult {
        return CheckResult(isNumber(input.current))
    }

    private fun String.parseNumber(reporter: ErrorsReporter, span: Span, type: TokenType): Number {
        return try {
            when (type) {
                TokenType.Integer -> toInt()
                TokenType.Float -> toFloat()
                TokenType.Double -> toDouble()
                TokenType.Long -> toLong()
                else -> throw IllegalStateException()
            }
        } catch (e: NumberFormatException) {
            reporter.reportInvalidLiteral(span, this)
            when (type) {
                TokenType.Integer -> 0
                TokenType.Float -> 0F
                TokenType.Double -> 0.0
                TokenType.Long -> 0L
                else -> throw IllegalStateException()
            }
        }
    }

    private fun isDecimalNumber(current: Char): Boolean {
        return isNumber(current) || current == '.' || isNumLiteralSuffix(current)
    }

    private fun isNumLiteralSuffix(current: Char): Boolean {
        return current == 'f' || current == 'F' || current == 'l' || current == 'L'
    }

    private fun isNumber(current: Char): Boolean {
        return current in '0'..'9'
    }
}