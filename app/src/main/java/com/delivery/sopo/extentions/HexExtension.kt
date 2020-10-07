package com.delivery.sopo.extentions

val ByteArray.asHex get() = byteToHexString(this)
val String.hexAsByteArray get() = hexStringToByteArray(this)


private val LOOKUP_TABLE_UPPER = charArrayOf(0x30.toChar(), 0x31.toChar(), 0x32.toChar(), 0x33.toChar(), 0x34.toChar(), 0x35.toChar(), 0x36.toChar(), 0x37.toChar(), 0x38.toChar(), 0x39.toChar(), 0x41.toChar(), 0x42.toChar(), 0x43.toChar(), 0x44.toChar(), 0x45.toChar(), 0x46.toChar())

private fun byteToHexString(byteArray: ByteArray): String
{
    val buffer = CharArray(byteArray.size shl 1)
    for (i in byteArray.indices)
    {
        val index = i shl 1
        val target = byteArray[i].toInt() and 0xFF

        buffer[index] = LOOKUP_TABLE_UPPER[target ushr 4]
        buffer[index + 1] = LOOKUP_TABLE_UPPER[target and 0x0F]
    }
    return String(buffer)
}


private fun hexStringToByteArray(s: String): ByteArray
{
    val len = s.length
    val data = ByteArray(len ushr 1)
    var i = 0
    while (i < len)
    {
        data[i ushr 1] = ((Character.digit(s[i], 16) shl 4) + Character.digit(s[i + 1], 16)).toByte()
        i += 2
    }
    return data
}