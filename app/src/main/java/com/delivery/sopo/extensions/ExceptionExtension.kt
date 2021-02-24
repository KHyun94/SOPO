package com.delivery.sopo.extensions

import android.content.Context
import com.delivery.sopo.R
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.enums.ResponseCode.*
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*

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

val Throwable?.match: ResponseCode get() = when(this)
{
    is FirebaseAuthWeakPasswordException -> FIREBASE_ERROR_WEAK_PASSWORD
    is FirebaseAuthInvalidUserException -> FIREBASE_ERROR_INVALIDATION_USER
    is FirebaseAuthInvalidCredentialsException ->  FIREBASE_ERROR_INVALID_CREDENTIALS
    is FirebaseTooManyRequestsException -> FIREBASE_ERROR_TOO_MANY_REQUESTS
    is FirebaseAuthUserCollisionException -> FIREBASE_ERROR_AUTH_USER_COLLISION
    is FirebaseAuthEmailException -> FIREBASE_ERROR_AUTH_EMAIL
    else -> FIREBASE_ERROR_UNKNOWN
}