package sash.emitter.scopes

import java.util.*

internal class LoopScope {

    private val stack = ArrayDeque<ArrayDeque<Index>>()

    internal fun startScope() = stack.push(ArrayDeque())

    internal inline fun endScope(breakBlock: (Int) -> Unit, continueBlock: (Int) -> Unit) {
        val current = stack.pop()
        while (current.isNotEmpty()) {
            when (val index = current.pop()) {
                is Index.Continue -> continueBlock(index.index)
                is Index.Break -> breakBlock(index.index)
            }
        }
    }

    internal fun addContinueIndex(index: Int) = stack.peek().push(Index.Continue(index))

    internal fun addBreakIndex(index: Int) = stack.peek().push(Index.Break(index))

    internal sealed class Index(internal val index: Int) {
        internal class Continue(index: Int) : Index(index)
        internal class Break(index: Int) : Index(index)
    }
}