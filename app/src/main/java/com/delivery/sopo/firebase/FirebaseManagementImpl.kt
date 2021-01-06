package com.delivery.sopo.firebase

import com.delivery.sopo.SOPOApp
import com.delivery.sopo.util.SopoLog
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.iid.FirebaseInstanceId


//todo kh firebase repository로 수정 예
object FirebaseManagementImpl : FirebaseManagement
{
    val TAG = "FirebaseUserManagement"

    override fun firebaseFCMResult(callback: FirebaseFCMCallback)
    {
        SopoLog.d(tag = TAG, msg = "firebaseFCMResult call()")

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener {
            callback.invoke(it)
        }
    }

    @Throws(FirebaseException::class)
    override fun firebaseCustomTokenLogin(token: String, callback : FirebaseCustomTokenLoginCallback)
    {
        SopoLog.d(tag = TAG, msg = "firebaseCustomTokenLogin call()")

        SOPOApp.auth.signInWithCustomToken(token).addOnCompleteListener {
            callback.invoke(it)
        }
    }

    @Throws(FirebaseException::class)
    override fun firebaseUpdateEmail(email: String, task : Task<AuthResult>, callback : FirebaseVoidCallback)
    {
        SopoLog.d(tag = TAG, msg = "firebaseUpdateEmail call()")
        if(task.result.user != null)
        {
            task.result.user!!.updateEmail(email).addOnCompleteListener {
                callback.invoke(it)
            }
        }
        else
        {
            callback.invoke(null)
        }
    }

    @Throws(FirebaseException::class)
    fun firebaseSendEmail(auth: FirebaseUser?, callback: FirebaseVoidCallback)
    {
        SopoLog.d(tag = TAG, msg = "firebaseUpdateEmail call()")
        if(auth == null)
        {
            callback.invoke(null)
            return
        }

        auth.sendEmailVerification().addOnCompleteListener {
            callback.invoke(it)
         return@addOnCompleteListener
        }
    }

    @Throws(FirebaseException::class)
    fun firebaseCreateUser(email: String, pwd: String): Task<AuthResult>
    {
        return SOPOApp.auth.createUserWithEmailAndPassword(email, pwd)
    }

    @Throws(FirebaseException::class)
    fun firebaseGeneralLogin(email: String, pwd: String): Task<AuthResult>
    {
        return SOPOApp.auth.signInWithEmailAndPassword(email, pwd)
    }




}