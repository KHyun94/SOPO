package com.delivery.sopo.util.fun_util

import android.util.Base64
import java.security.GeneralSecurityException
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AESHelper {
    /** 키를 외부에 저장할 경우 유출 위험이 있으니까 소스 코드 내에 숨겨둔다. 길이는 16자여야 한다. */
    private const val SECRET_KEY = "HelloWorld!!@#$%"
    private const val CIPHER_TRANSFORMATION = "AES/CBC/PKCS5PADDING"

    fun encrypt(plainText: String, initVector: String): String {
        val cipherText = try {
            with(Cipher.getInstance(CIPHER_TRANSFORMATION), {
                init(Cipher.ENCRYPT_MODE,
                    SecretKeySpec(SECRET_KEY.toByteArray(), "AES"),
                    IvParameterSpec(initVector.toByteArray())
                )
                return@with doFinal(plainText.toByteArray())
            })
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
            ""
        }

        return Base64.encodeToString(cipherText as ByteArray?, Base64.DEFAULT)
    }

    fun decrypt(base64CipherText: String, initVector: String): String {
        val plainTextBytes = try {
            with(Cipher.getInstance(CIPHER_TRANSFORMATION), {
                init(Cipher.DECRYPT_MODE,
                    SecretKeySpec(SECRET_KEY.toByteArray(), "AES"),
                    IvParameterSpec(initVector.toByteArray()))
                val cipherText = Base64.decode(base64CipherText, Base64.DEFAULT)
                return@with doFinal(cipherText)
            })
        } catch (e: GeneralSecurityException) {
            e.printStackTrace()
            ByteArray(0, { i -> 0 })
        }

        return String(plainTextBytes)
    }
}