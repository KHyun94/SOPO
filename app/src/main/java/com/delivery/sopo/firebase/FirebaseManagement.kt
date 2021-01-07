package com.delivery.sopo.firebase

import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.iid.InstanceIdResult

typealias FirebaseVoidCallback = (Task<Void>?) -> Unit
typealias FirebaseLoginCallback = (Task<AuthResult>) -> Unit
typealias FirebaseFCMCallback = (Task<InstanceIdResult>) -> Unit

interface FirebaseManagement
{
    @Throws(FirebaseException::class)
    fun firebaseFCMResult(callback: FirebaseFCMCallback)
    @Throws(FirebaseException::class)
    fun firebaseCustomTokenLogin(token: String, callback : FirebaseLoginCallback)
    @Throws(FirebaseException::class)
    fun firebaseUpdateEmail(email : String, task : Task<AuthResult>, callback : FirebaseVoidCallback)
}