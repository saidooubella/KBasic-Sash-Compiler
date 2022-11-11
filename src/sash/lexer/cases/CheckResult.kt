package sash.lexer.cases

internal class CheckResult private constructor(internal val success: Boolean, internal val extra: Any?) {
    companion object {

        private val SUCCESS = CheckResult(true, null)
        private val FAILURE = CheckResult(false, null)

        internal operator fun invoke(result: Boolean, extra: Any? = null): CheckResult {
            return if (result) if (extra == null) SUCCESS else CheckResult(true, extra) else FAILURE
        }
    }
}