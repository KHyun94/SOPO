package com.delivery.sopo.extensions

import java.security.MessageDigest
import java.text.SimpleDateFormat

val String.asSHA256 get() = SHA256(this)

fun String.removeSpace(): String
{
    return this.replace(" ", "")
}

operator fun String.get(range: IntRange): String
{
    return this.substring(range)
}

fun String.md5(): String
{
    val md = MessageDigest.getInstance("MD5")
    val bytes = md.digest(this.toByteArray())
    return bytes.asHex
}

fun SHA256(string: String): String
{
    val bytes = string.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)

    return digest.asHex
}

fun String.toMilliSeconds(): Long?
{
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")

    try
    {
        val date = sdf.parse(this)
        return date.time
    }
    catch (e: Exception)
    {
        return null
    }
}

// 파라미터 값보다 같거나 크면 true 이외 false
fun String?.isGreaterThanOrEqual(minLength : Int) : Boolean
{
    if(this == null) return false
    return this.length >= minLength
}

// 파라미터 값보다 작으면 true 이외 false
fun String?.isLessThan(maxLength : Int) : Boolean
{
    if(this == null) return false
    return this.length < maxLength
}