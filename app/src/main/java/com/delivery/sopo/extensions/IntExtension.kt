package com.delivery.sopo.extensions

import java.security.MessageDigest
import java.text.SimpleDateFormat

fun Int.asEmoji(): String { return String(Character.toChars(this)) }