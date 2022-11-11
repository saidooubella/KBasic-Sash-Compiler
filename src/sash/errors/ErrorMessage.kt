package sash.errors

import sash.span.Span

internal data class ErrorMessage(internal val span: Span, internal val message: String) {
    internal val formattedMessage: String = run {
        val (startLine, startColumn) = span.start
        val (endLine, endColumn) = span.end
        "($startLine:$startColumn, $endLine:$endColumn) $message."
    }
}
