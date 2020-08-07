package com.delivery.sopo.firebase

import com.delivery.sopo.SOPOApp
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser

typealias FirebaseAuthCallback = (Task<AuthResult>?) -> Unit
typealias SendEmailCallback = (Task<Void>?) -> Unit

object FirebaseUserManagement {

    @Throws(FirebaseException::class)
    fun firebaseCreateUser(email: String, pwd: String): Task<AuthResult> {
        return SOPOApp.auth.createUserWithEmailAndPassword(email, pwd)
    }

    @Throws(FirebaseException::class)
    fun firebaseGeneralLogin(email: String, pwd: String): Task<AuthResult> {
        return SOPOApp.auth.signInWithEmailAndPassword(email, pwd)
    }

    @Throws(FirebaseException::class)
    fun firebaseSendEmail(auth: FirebaseUser?): Task<Void>? {
        return auth?.sendEmailVerification()
    }

    @Throws(FirebaseException::class)
    fun firebaseCustomTokenLogin(token: String): Task<AuthResult> {
        return SOPOApp.auth.signInWithCustomToken(token)
    }
}