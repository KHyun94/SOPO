package com.delivery.sopo.firebase

import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.SuccessResult
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.iid.InstanceIdResult

typealias FirebaseVoidCallback = (SuccessResult<String?>?, ErrorResult<String?>?) -> Unit
typealias FirebaseUserCallback = (SuccessResult<FirebaseUser?>?, ErrorResult<String?>?) -> Unit
typealias FirebaseFCMCallback = (Task<InstanceIdResult>) -> Unit

interface FirebaseDataSource
{
    fun createFirebaseAccount(email: String, password: String, callback: FirebaseUserCallback)
    fun sendFirebaseAuthEmail(auth : FirebaseUser, callback: FirebaseVoidCallback)
    /** Firebase Login With Email And Password */
    fun firebaseSelfLogin(email: String, password: String, callback : FirebaseUserCallback)

    @Throws(FirebaseException::class)
    fun firebaseFCMResult(callback: FirebaseFCMCallback)

    @Throws(FirebaseException::class)
    fun loginFirebaseWithCustomToken(email : String, token: String, callback: FirebaseUserCallback)

    @Throws(FirebaseException::class)
    fun updateFirebaseEmail(email: String, task: FirebaseUser, callback: FirebaseVoidCallback)


}