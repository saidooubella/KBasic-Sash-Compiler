package sash.input.providers

import sash.errors.ErrorsReporter
import sash.input.CharInput
import sash.input.InputProvider
import sash.lexer.cases.tokens.*
import sash.lexer.cases.trivias.*
import sash.lexer.token.TokenInfo
import sash.lexer.token.TokenType

private val TOKEN_CASES = listOf(
    EndOfFileCase,
    // Trivias
    WhitespaceCase,
    LineBreakCase,
    BlockCommentCase,
    LineCommentCase,
    // --------------
    IdentifierCase,
    NumberCase,
    StringCase,
    CharCase,
    PunctuationCase,
    // Tokens
    IllegalCharCase
)

internal class TokenInfoProvider(
    private val input: CharInput,
    private val reporter: ErrorsReporter
) : InputProvider<TokenInfo, TokenInfo> {

    override val emptyValue: TokenInfo = TokenInfo.Empty

    override fun isEndReached(item: TokenInfo): Boolean = item.type == TokenType.EndOfFile

    override fun nextItem(): TokenInfo = createToken()

    override fun map(item: TokenInfo): TokenInfo = item

    override fun close() = input.close()

    private fun createToken(): TokenInfo {
        for (case in TOKEN_CASES) {
            val result = case.check(input)
            if (!result.success) continue
            return case.perform(input, reporter, result.extra)
        }
        throw IllegalStateException("Unreachable")
    }
}