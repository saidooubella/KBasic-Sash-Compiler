package sash.lexer.cases.tokens

import sash.errors.ErrorsReporter
import sash.input.CharInput
import sash.input.providers.contains
import sash.lexer.cases.CheckResult
import sash.lexer.cases.TokenCase
import sash.lexer.token.TokenInfo
import sash.lexer.token.TokenType
import sash.span.Span

internal object PunctuationCase : TokenCase {

    override fun perform(input: CharInput, reporter: ErrorsReporter, extra: Any?): TokenInfo {
        val repr = extra as Punctuation
        val start = input.position()
        input.advanceBy(repr.repr.length)
        val end = input.position()
        val span = Span(start, end)
        return TokenInfo(repr.repr, repr.type, null, span)
    }

    override fun check(input: CharInput): CheckResult {
        return Punctuation.PUNCTUATION.firstOrNull { it.repr in input }
            ?.let { CheckResult(true, it) } ?: CheckResult(false)
    }

    private class Punctuation private constructor(val repr: String, val type: TokenType) {
        companion object {
            val PUNCTUATION = listOf(
                // Double character punctuation
                Punctuation("&&", TokenType.AmpersandAmpersand),
                Punctuation(">=", TokenType.GreaterThanEqual),
                Punctuation("<=", TokenType.LessThanEqual),
                Punctuation("==", TokenType.EqualEqual),
                Punctuation("!=", TokenType.BangEqual),
                Punctuation("||", TokenType.PipePipe),
                // Single character punctuation
                Punctuation(")", TokenType.CloseParentheses),
                Punctuation("(", TokenType.OpenParentheses),
                Punctuation(">", TokenType.GreaterThan),
                Punctuation("}", TokenType.CloseCurly),
                Punctuation("{", TokenType.OpenCurly),
                Punctuation("<", TokenType.LessThan),
                Punctuation("?", TokenType.Question),
                Punctuation(":", TokenType.Colon),
                Punctuation(",", TokenType.Comma),
                Punctuation("=", TokenType.Equal),
                Punctuation("-", TokenType.Minus),
                Punctuation("/", TokenType.Slash),
                Punctuation("!", TokenType.Bang),
                Punctuation("+", TokenType.Plus),
                Punctuation("*", TokenType.Star)
            )
        }
    }
}