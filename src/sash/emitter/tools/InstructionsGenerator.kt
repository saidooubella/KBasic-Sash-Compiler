package sash.emitter.tools

import java.io.BufferedWriter
import java.io.FileWriter

private enum class TargetFile(private val path: String) {

    KtInstructions("C:\\Users\\Said\\IdeaProjects\\Basic-Sash\\src\\sash\\emitter\\Instructions.kt") {
        override fun write(writer: BufferedWriter) = writer.generateKtInstructionsFile("sash.emitter")
    },

    KtConstPoolTags("C:\\Users\\Said\\IdeaProjects\\Basic-Sash\\src\\sash\\emitter\\consts\\ConstsTags.kt") {
        override fun write(writer: BufferedWriter) = writer.generateKtConstsTagsFile("sash.emitter.consts")
    },

    GoInstructions("C:\\Users\\Said\\Desktop\\DemoGo\\bytecode\\opcodes.go") {
        override fun write(writer: BufferedWriter) = writer.generateGoInstructionsFile("bytecode")
    },

    GoConstPoolTags("C:\\Users\\Said\\Desktop\\DemoGo\\bytecode\\consttags.go") {
        override fun write(writer: BufferedWriter) = writer.generateGoConstsTagsFile("bytecode")
    },

    GoOpcodesDef("C:\\Users\\Said\\Desktop\\DemoGo\\disassembler\\opcodesdefs.go") {
        override fun write(writer: BufferedWriter) = writer.generateGoOpcodesDefsFile("disassembler")
    };

    fun generate() = FileWriter(path).buffered().use { writer -> write(writer) }

    protected abstract fun write(writer: BufferedWriter)
}

private class OpCode(val name: String, val operands: IntArray)

private val opcodes = arrayOf(

    OpCode("GOTO_FALSE_OR_DROP", intArrayOf(-2)),
    OpCode("GOTO_TRUE_OR_DROP", intArrayOf(-2)),
    OpCode("GOTO_DROP_FALSE", intArrayOf(-2)),
    OpCode("GOTO_DROP_TRUE", intArrayOf(-2)),
    OpCode("GOTO", intArrayOf(-2)),

    OpCode("CLOSE_FREE", intArrayOf()),
    OpCode("PUSH_FALSE", intArrayOf()),
    OpCode("PUSH_TRUE", intArrayOf()),

    OpCode("ADD", intArrayOf()),
    OpCode("SUB", intArrayOf()),
    OpCode("MUL", intArrayOf()),
    OpCode("DIV", intArrayOf()),
    OpCode("NEG", intArrayOf()),

    OpCode("GET_GLOBAL", intArrayOf(2)),
    OpCode("SET_GLOBAL", intArrayOf(2)),
    OpCode("GET_FREE", intArrayOf(2)),
    OpCode("SET_FREE", intArrayOf(2)),
    OpCode("GET_LOCAL", intArrayOf(2)),
    OpCode("SET_LOCAL", intArrayOf(2)),

    OpCode("RETURN", intArrayOf()),
    OpCode("CALL", intArrayOf()),

    OpCode("GREATER", intArrayOf()),
    OpCode("EQUALS", intArrayOf()),
    OpCode("LESS", intArrayOf()),
    OpCode("NOT", intArrayOf()),

    OpCode("CONCAT", intArrayOf(2)),
    OpCode("PRINT", intArrayOf()),

    OpCode("CONSTANT", intArrayOf(2)),
    OpCode("CLOSURE", intArrayOf(2)),
    OpCode("POP", intArrayOf()),
    OpCode("HALT", intArrayOf())
)

private val constsTags = arrayOf(
    "TAG_INTEGER_CONST",
    "TAG_DOUBLE_CONST",
    "TAG_STRING_CONST",
    "TAG_FLOAT_CONST",
    "TAG_LONG_CONST",
    "TAG_FUN_CONST"
)

fun main() {
    check(opcodes.size <= 256) { "Reached bytecode limit size" }
    TargetFile.values().forEach(TargetFile::generate)
    println("Done!")
}

private fun BufferedWriter.generateKtInstructionsFile(pkgName: String) {

    val opcodesSize = opcodes.size

    append("package ")
    appendln(pkgName)
    newLine()

    appendln("// AUTO GENERATED - DO NOT MODIFY")
    appendln("internal object Instructions {")

    for (index in 0 until opcodesSize) {
        val opcode = opcodes[index]
        append("\tinternal const val ")
        append(opcode.name.toUpperCase())
        append(": Byte = ")
        append(index.toByte().toString())
        newLine()
    }

    append('}')
}

private fun BufferedWriter.generateKtConstsTagsFile(pkgName: String) {

    val opcodesSize = constsTags.size

    append("package ")
    appendln(pkgName)
    newLine()

    appendln("// AUTO GENERATED - DO NOT MODIFY")
    appendln("internal object ConstsTags {")

    for (index in 0 until opcodesSize) {
        val tag = constsTags[index]
        append("\tinternal const val ")
        append(tag.toUpperCase())
        append(": Byte = ")
        append(index.toByte().toString())
        newLine()
    }

    append('}')
}

private fun BufferedWriter.generateGoInstructionsFile(pkgName: String) {

    val opcodesSize: Int = opcodes.size

    appendln("// AUTO GENERATED - DO NOT MODIFY")
    append("package ")
    appendln(pkgName)
    newLine()

    appendln("type OpCode uint8")
    newLine()

    appendln("const (")

    for (index in 0 until opcodesSize) {
        val opcode = opcodes[index]
        append("\tOP_")
        append(opcode.name.toUpperCase())
        if (index == 0) append(" OpCode = iota")
        appendln()
    }

    appendln(")")
}

private fun BufferedWriter.generateGoConstsTagsFile(pkgName: String) {

    val opcodesSize: Int = constsTags.size

    appendln("// AUTO GENERATED - DO NOT MODIFY")
    append("package ")
    appendln(pkgName)
    newLine()

    appendln("type ConstTag uint8")
    newLine()

    appendln("const (")

    for (index in 0 until opcodesSize) {
        val constTag = constsTags[index]
        append('	').append(constTag.toUpperCase())
        if (index == 0) append(" ConstTag = iota")
        appendln()
    }

    appendln(")")
}

private fun BufferedWriter.generateGoOpcodesDefsFile(pkgName: String) {

    val opcodesSize = opcodes.size

    appendln("// AUTO GENERATED - DO NOT MODIFY")
    append("package ").appendln(pkgName)
    newLine()

    // ----------

    appendln("type instruction struct {")
    appendln("\tname string")
    appendln("\toperands []int8")
    appendln("}")
    newLine()

    // ----------

    appendln("var instructions = [...]instruction{")

    for (index in 0 until opcodesSize) {
        val opcode = opcodes[index]
        append('\t')
        append("{\"op_")
        append(opcode.name.toLowerCase())
        append("\", []int8{")
        append(opcode.operands.joinToString(separator = ", "))
        append("}}")
        appendln(',')
    }

    appendln('}')

    // ----------
}
