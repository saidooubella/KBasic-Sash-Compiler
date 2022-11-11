package sash.input.providers

import sash.errors.ErrorsReporter
import sash.input.CharInput
import sash.input.Input
import sash.input.InputProvider
import sash.lexer.token.*

internal class TokenProvider(input: CharInput, reporter: ErrorsReporter) : InputProvider<Token, Token> {

    private val tokensStream = Input(TokenInfoProvider(input, reporter))

    override val emptyValue: Token = Token.Empty

    override fun isEndReached(item: Token): Boolean = item.type == TokenType.EndOfFile

    override fun nextItem(): Token = createToken()

    override fun map(item: Token): Token = item

    override fun close() = tokensStream.close()

    private fun createToken(): Token {
        val leading = createTrivia(false)
        val token = tokensStream.consume()
        val trailing = createTrivia(true)
        return token.run { Token(text, type, value, span, leading, trailing) }
    }

    private fun createTrivia(isTrailing: Boolean): List<Trivia> {
        val trivias = mutableListOf<Trivia>()
        while (!tokensStream.isFinished && tokensStream.current.type.isTrivia) {
            val trivia = tokensStream.consume()
            trivias += trivia.run { Trivia(text, type, span) }
            if (isTrailing && trivia.type == TokenType.LineBreak) break
        }
        return trivias
    }
}