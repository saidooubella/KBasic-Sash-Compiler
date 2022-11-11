package sash.emitter.tools

fun ByteArray.write1Byte(offset: Int, value: Byte) {
    this[offset] = value
}

fun ByteArray.write2Byte(offset: Int, value: Short) {
    this[offset] = (0xff and (value.toInt() shr 8)).toByte()
    this[offset + 1] = (0xff and (value.toInt())).toByte()
}

fun ByteArray.write4Byte(offset: Int, value: Int) {
    this[offset] = (0xff and (value shr 24)).toByte()
    this[offset + 1] = (0xff and (value shr 16)).toByte()
    this[offset + 2] = (0xff and (value shr 8)).toByte()
    this[offset + 3] = (0xff and (value)).toByte()
}

fun ByteArray.write8Byte(offset: Int, value: Long) {
    this[offset] = (0xffL and (value shr 56)).toByte()
    this[offset + 1] = (0xffL and (value shr 48)).toByte()
    this[offset + 2] = (0xffL and (value shr 40)).toByte()
    this[offset + 3] = (0xffL and (value shr 32)).toByte()
    this[offset + 4] = (0xffL and (value shr 24)).toByte()
    this[offset + 5] = (0xffL and (value shr 16)).toByte()
    this[offset + 6] = (0xffL and (value shr 8)).toByte()
    this[offset + 7] = (0xffL and (value)).toByte()
}
