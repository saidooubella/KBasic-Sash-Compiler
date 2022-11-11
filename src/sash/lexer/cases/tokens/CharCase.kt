package sash.lexer.cases.tokens

import sash.errors.ErrorsReporter
import sash.input.CharInput
import sash.lexer.cases.CheckResult
import sash.lexer.cases.TokenCase
import sash.lexer.token.TokenInfo
import sash.lexer.token.TokenType
import sash.span.Span

internal object CharCase : TokenCase {

    override fun perform(input: CharInput, reporter: ErrorsReporter, extra: Any?): TokenInfo {

        val textBuilder = StringBuilder()
        val charBuilder = StringBuilder()

        val start = input.position()
        textBuilder.append(input.consume())

        var isEnded = false

        while (!input.isFinished && input.isNextNotNewLine()) {

            if (input.current == '\\') {
                input.escapeChar(reporter, textBuilder)
                    ?.also { charBuilder.append(it) }
                continue
            }

            val current = input.consume()

            if (current == '\'') {
                textBuilder.append(current)
                isEnded = true
                break
            }

            textBuilder.append(current)
            charBuilder.append(current)
        }

        val end = input.position()
        val span = Span(start, end)
        val text = textBuilder.toString()
        val char = charBuilder.toString()

        if (char.isEmpty()) reporter.reportEmptyCharLiteral(span)
        else if (char.length > 1) reporter.reportTooManyCharacterInCharLiteral(span)

        if (!isEnded) reporter.reportUnterminatedCharLiteral(Span(span.end, span.end))

        return TokenInfo(text, TokenType.Character, char.getOrNull(0) ?: '\u0000', span)
    }

    override fun check(input: CharInput): CheckResult {
        return CheckResult(input.current == '\'')
    }
}

internal fun CharInput.isNextNotNewLine(): Boolean {
    return !(current == '\n' || current == '\r' && peek(1) == '\n')
}

internal fun CharInput.escapeChar(reporter: ErrorsReporter, textBuilder: StringBuilder): Char? {

    val escBuilder = StringBuilder()

    val posStart = position()
    escBuilder.append(consume())

    val escChar = consume()
    escBuilder.append(escChar)

    val actual = escChar.toEscapedChar() ?: escapeHexChar(escChar, escBuilder)

    val escText = escBuilder.toString()

    if (actual == null) {
        val span = Span(posStart, position())
        reporter.reportIllegalEscape(span, escText)
    }

    textBuilder.append(escText)

    return actual
}

private fun CharInput.escapeHexChar(escChar: Char, escBuilder: StringBuilder): Char? {

    if (escChar != 'u') return null

    var char = 0

    for (i in 0 until 4) {
        if (current.isHexDigit()) {
            val curr = consume()
            char = char * 16 + Character.digit(curr, 16)
            escBuilder.append(curr)
        } else return null
    }

    return char.toChar()
}


private fun Char.toEscapedChar(): Char? {
    return when (this) {
        '"' -> '"'
        't' -> '\t'
        'n' -> '\n'
        'b' -> '\b'
        'r' -> '\r'
        '0' -> '\u0000'
        '\'' -> '\''
        '\\' -> '\\'
        else -> null
    }
}

private fun Char.isHexDigit(): Boolean {
    return this in '0'..'9' || this in 'a'..'f' || this in 'A'..'F'
}
