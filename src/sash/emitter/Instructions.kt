package sash.emitter

// AUTO GENERATED - DO NOT MODIFY
internal object Instructions {
	internal const val GOTO_FALSE_OR_DROP: Byte = 0
	internal const val GOTO_TRUE_OR_DROP: Byte = 1
	internal const val GOTO_DROP_FALSE: Byte = 2
	internal const val GOTO_DROP_TRUE: Byte = 3
	internal const val GOTO: Byte = 4
	internal const val CLOSE_FREE: Byte = 5
	internal const val PUSH_FALSE: Byte = 6
	internal const val PUSH_TRUE: Byte = 7
	internal const val ADD: Byte = 8
	internal const val SUB: Byte = 9
	internal const val MUL: Byte = 10
	internal const val DIV: Byte = 11
	internal const val NEG: Byte = 12
	internal const val GET_GLOBAL: Byte = 13
	internal const val SET_GLOBAL: Byte = 14
	internal const val GET_FREE: Byte = 15
	internal const val SET_FREE: Byte = 16
	internal const val GET_LOCAL: Byte = 17
	internal const val SET_LOCAL: Byte = 18
	internal const val RETURN: Byte = 19
	internal const val CALL: Byte = 20
	internal const val GREATER: Byte = 21
	internal const val EQUALS: Byte = 22
	internal const val LESS: Byte = 23
	internal const val NOT: Byte = 24
	internal const val CONCAT: Byte = 25
	internal const val PRINT: Byte = 26
	internal const val CONSTANT: Byte = 27
	internal const val CLOSURE: Byte = 28
	internal const val POP: Byte = 29
	internal const val HALT: Byte = 30
}