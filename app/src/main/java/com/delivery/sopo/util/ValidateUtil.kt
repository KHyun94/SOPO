package com.delivery.sopo.util

import android.util.Patterns
import java.text.SimpleDateFormat
import java.util.regex.Pattern

object ValidateUtil {
    val TAG = "LOG.SOPO" + this.javaClass.simpleName

    // 이메일 정규식 체크
    fun isValidateEmail(email: String?): Boolean {
        return if (email == null) {
            false
        } else {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }

    // 비밀번호 정규식 체크
    fun isValidatePassword(pwd: String?): Boolean {
        return if (pwd == null) {
            false
        } else {
            val matcher = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[\$~@\$!%*^#?&])[A-Za-z\\d\$~@\$!%*#?&]{8,15}\$").matcher(pwd)
            matcher.matches()
        }
    }

    fun isValidateDateFormat(date: String): Boolean
    {
        return try
        {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            sdf.isLenient = false
            sdf.parse(date)
            true
        }
        catch (e: Exception)
        {
            false
        }
    }
}