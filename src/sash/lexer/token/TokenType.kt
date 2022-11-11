package sash.lexer.token

internal sealed class TokenType {
    // Trivias
    internal object Whitespace : TokenType()
    internal object LineBreak : TokenType()
    internal object LineComment : TokenType()
    internal object BlockComment : TokenType()
    internal object IllegalChar : TokenType()
    // Punctuation
    internal object Plus : TokenType()
    internal object Minus : TokenType()
    internal object Slash : TokenType()
    internal object Star : TokenType()
    internal object PipePipe : TokenType()
    internal object AmpersandAmpersand : TokenType()
    internal object EqualEqual : TokenType()
    internal object BangEqual : TokenType()
    internal object GreaterThanEqual : TokenType()
    internal object LessThanEqual : TokenType()
    internal object GreaterThan : TokenType()
    internal object LessThan : TokenType()
    internal object Bang : TokenType()
    internal object Equal : TokenType()
    internal object Colon : TokenType()
    internal object Comma : TokenType()
    internal object Question : TokenType()
    internal object OpenCurly : TokenType()
    internal object CloseCurly : TokenType()
    internal object OpenParentheses : TokenType()
    internal object CloseParentheses : TokenType()
    // Keywords
    internal object TrueKeyword : TokenType()
    internal object FalseKeyword : TokenType()
    internal object LetKeyword : TokenType()
    internal object DefKeyword : TokenType()
    internal object FunKeyword : TokenType()
    internal object IfKeyword : TokenType()
    internal object ElseKeyword : TokenType()
    internal object WhileKeyword : TokenType()
    internal object DoKeyword : TokenType()
    internal object BreakKeyword : TokenType()
    internal object ReturnKeyword : TokenType()
    internal object ContinueKeyword : TokenType()
    internal object PrintKeyword : TokenType()
    // Identifier and literals
    internal object Identifier : TokenType()
    internal object Integer : TokenType()
    internal object Double : TokenType()
    internal object Long : TokenType()
    internal object Float : TokenType()
    internal object String : TokenType()
    internal object Character : TokenType()
    // End of file
    internal object EndOfFile : TokenType()
}

internal val TokenType.isTrivia: Boolean
    get() = when (this) {
        TokenType.BlockComment -> true
        TokenType.IllegalChar -> true
        TokenType.LineComment -> true
        TokenType.LineBreak -> true
        TokenType.Whitespace -> true
        else -> false
    }
