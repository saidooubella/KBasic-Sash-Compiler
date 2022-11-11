package sash

import sash.binder.Binder
import sash.binder.nodes.ProgramBindNode
import sash.errors.ErrorReports
import sash.errors.ErrorsReporter
import sash.input.CharInput
import sash.input.Input
import sash.input.providers.ReaderProvider
import sash.input.providers.TokenProvider
import sash.parser.Parser
import java.io.Reader

@Suppress("FunctionName", "MoveLambdaOutsideParentheses")
internal fun Compiler(reader: Reader): CompilerResult {
    val errors = ErrorsReporter()
    val charStream = CharInput(ReaderProvider(reader.buffered()))
    val lexer = Input(TokenProvider(charStream, errors))
    val ast = Parser(lexer, errors).use(Parser::createProgramNode)
    val program = Binder(ast, errors).createProgramNode()
    return CompilerResult(program, errors.asErrorReports)
}

internal data class CompilerResult(val program: ProgramBindNode, val errors: ErrorReports)
