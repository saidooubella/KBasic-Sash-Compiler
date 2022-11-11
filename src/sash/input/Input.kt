package sash.input

internal class Input<I : Any, R : Any>(
    provider: InputProvider<I, R>
) : AbstractInput<I, R>(provider) {
    override fun onPreNext(current: R) = Unit
}
