package sash

import sash.emitter.Emitter
import java.io.FileOutputStream
import java.io.FileReader

private const val TEST_INPUT_PATH = "C:\\Users\\Said\\IdeaProjects\\Basic-Sash\\src\\sash\\main.sash"
private const val TEST_OUTPUT_PATH = "C:\\Users\\Said\\Desktop\\main.shp"

fun main() {
    val fileReader = FileReader(TEST_INPUT_PATH)
    val (program, errors) = Compiler(fileReader)
    errors.forEach { println(it.formattedMessage) }
    if (errors.isEmpty) {
        FileOutputStream(TEST_OUTPUT_PATH).buffered().use { output ->
            Emitter(program).writeTo(output)
        }
    }
}
