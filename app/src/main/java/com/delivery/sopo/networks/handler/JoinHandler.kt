package com.delivery.sopo.networks.handler

import com.delivery.sopo.SOPOApp
import com.delivery.sopo.extensions.match
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.networks.call.JoinCall
import com.delivery.sopo.networks.datasource.JoinCallback
import com.delivery.sopo.services.network_handler.NetworkResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object JoinHandler
{
    fun requestJoinBySelf(email : String, password : String, deviceInfo : String, callback: JoinCallback)
    {
        FirebaseRepository.firebaseCreateUser(email = email, password = password){ success, error ->
            if(error != null) return@firebaseCreateUser

            if(success != null)
            {
                val firebaseUser = success.data

                CoroutineScope(Dispatchers.IO).launch {
                    when(val result = JoinCall.requestJoinBySelf(email, password, deviceInfo, firebaseUser?.uid?:""))
                    {
                        is NetworkResult.Success ->
                        {
                            FirebaseRepository.sendFirebaseAuthEmail(SOPOApp.auth.currentUser!!){success, error ->
                                callback.invoke(success, error)
                            }
                        }
                        is NetworkResult.Error ->
                        {
                            val exception = result.exception
                            val errorCode = exception.match

                            callback.invoke(null, ErrorResult(errorCode, errorCode.MSG, ErrorResult.ERROR_TYPE_DIALOG, null, exception))
                        }
                    }
                }

            }
        }
    }
}