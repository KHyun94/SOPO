package com.delivery.sopo.extensions

import android.content.Context
import androidx.core.content.ContextCompat
import java.security.MessageDigest
import java.text.SimpleDateFormat

fun Int.asEmoji(): String { return String(Character.toChars(this)) }
fun Int.toColorRes(context: Context): Int { return ContextCompat.getColor(context, this) }