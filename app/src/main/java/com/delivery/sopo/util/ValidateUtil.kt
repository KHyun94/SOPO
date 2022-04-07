package com.delivery.sopo.util

import android.util.Patterns
import java.text.SimpleDateFormat
import java.util.regex.Pattern

object ValidateUtil
{

    // 이메일 정규식 체크
    fun isValidateEmail(email: String?): Boolean
    {
        return if(email == null)
        {
            false
        }
        else
        {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }
    }

    // 비밀번호 정규식 체크
    fun isValidatePassword(pwd: String?): Boolean
    {
        return if(pwd == null)
        {
            false
        }
        else
        {
//            val matcher = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[\$~@\$!%*^#?&])[A-Za-z\\d\$~@\$!%*#?&]{8,15}\$").matcher(pwd)
            val matcher = Pattern.compile("^[a-zA-Z0-9]{8,15}\$").matcher(pwd)
            matcher.matches()
        }
    }

    fun isPassMinNumber(min: Int, word: String?): Boolean
    {
        if(word == null) return false
        return min <= word.length
    }

    fun hasNumber(word: String?): Boolean
    {
        if(word == null)
        {
            return false
        }

        var index = 0

        while(index < word.length)
        {
            val char = word[index]

            if(char.isDigit())
            {
                return true
            }

            index++
        }

        return false
    }

    fun hasEnglish(word: String?): Boolean
    {
        if(word == null)
        {
            return false
        }

        var index = 0

        while(index < word.length)
        {
            val char = word[index]

            if((char >= 0x41.toChar() && char <= 0x51.toChar()) || (char >= 0x61.toChar() && char <= 0x7A.toChar()))
            {
                return true
            }
            index++
        }

        return false
    }

    fun isValidateNickname(nickname: String) =
        Pattern.compile("^[a-zA-Z0-9가-힣]+$").matcher(nickname).matches() && nickname.length <= 12

    fun isValidateDateFormat(date: String): Boolean
    {
        return try
        {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS")
            sdf.isLenient = false
            sdf.parse(date)
            true
        }
        catch(e: Exception)
        {
            false
        }
    }
}