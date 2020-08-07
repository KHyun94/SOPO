package com.delivery.sopo.extentions

import android.content.Context
import com.delivery.sopo.R
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException

fun Throwable?.getCommonMessage(context: Context): String
{
    return context.getString(this.commonMessageResId)
}

val Throwable?.commonMessageResId: Int get() = when(this)
{
    is FirebaseAuthWeakPasswordException -> R.string.ERROR_MESSAGE_WEAK_PASSWORD
    is FirebaseAuthInvalidUserException -> R.string.ERROR_MESSAGE_INVALID_USER
    is FirebaseAuthInvalidCredentialsException ->  R.string.ERROR_MESSAGE_WRONG_LOGIN
    is FirebaseTooManyRequestsException -> R.string.ERROR_MESSAGE_TOO_MANY_REQUEST
    is FirebaseAuthUserCollisionException -> R.string.ERROR_MESSAGE_ALREADY_EXIST
    else -> R.string.ERROR_MESSAGE_UNKNOWN_ERROR
}