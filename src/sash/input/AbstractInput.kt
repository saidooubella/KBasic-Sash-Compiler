package sash.input

import java.util.*
import kotlin.math.abs

internal abstract class AbstractInput<I : Any, R : Any>(
    private val provider: InputProvider<I, R>
) : AutoCloseable {

    private lateinit var _current: R

    private var _isFinished: Boolean = false
    private var _isStarted: Boolean = false

    private var _cache: LinkedList<R>? = null

    private val cache: LinkedList<R>
        get() = _cache ?: LinkedList<R>().also { _cache = it }

    internal val isFinished: Boolean get() = _isFinished
    internal val current: R get() = _current

    init {
        advance()
    }

    internal abstract fun onPreNext(current: R)

    internal fun advance() {

        if (_isFinished) return

        if (_isStarted) onPreNext(_current) else _isStarted = true

        _current = if (cache.isEmpty()) {
            val next = provider.nextItem()
            if (provider.isEndReached(next)) {
                _isFinished = true
                provider.emptyValue
            } else provider.map(next)
        } else cache.removeFirst()
    }

    internal fun advanceBy(steps: Int) {
        require(steps > 0) { "steps <= 0" }
        if (steps == 1) advance() else repeat(steps) { advance() }
    }

    internal fun peek(offset: Int): R {

        require(offset >= 0) { "offset < 0." }

        if (isFinished || offset == 0)
            return current

        if (cache.isNotEmpty() && offset - 1 < cache.size)
            return cache[offset - 1]

        repeat(if (cache.isEmpty()) offset else abs(cache.size - offset)) {
            val item = provider.nextItem()
            if (provider.isEndReached(item))
                return provider.emptyValue
            cache.addLast(provider.map(item))
        }

        return cache.last
    }

    internal fun consume() = current.also { advance() }

    final override fun close() = provider.close()
}