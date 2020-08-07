package com.delivery.sopo.interfaces

import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult

interface FirebaseCallback {
     fun onSuccess(cb:(Task<AuthResult>)->Unit)
    fun onError(cb:(Task<AuthResult>)->Unit)
}