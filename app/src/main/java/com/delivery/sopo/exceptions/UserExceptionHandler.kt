package com.delivery.sopo.exceptions

import android.content.Intent
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.OAuthException
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.services.receivers.LogOutBroadcastReceiver
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.views.intro.IntroView
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

class UserExceptionHandler(private val dispatcher: CoroutineDispatcher, private val callback: OnSOPOErrorCallback):
        CoroutineExceptionHandler
{
    private val logOutBroadcastReceiver: LogOutBroadcastReceiver by lazy { LogOutBroadcastReceiver() }

    override val key: CoroutineContext.Key<*> = dispatcher.key

    override fun handleException(context: CoroutineContext, exception: Throwable)
    {
        SopoLog.e("UseExceptionHandler -> ${exception.toString()}")

        when(exception)
        {
            is SOPOApiException ->
            {
                val errorCode = ErrorEnum.getErrorCode(exception.getErrorResponse().code).apply {
                    message = exception.getErrorResponse().message
                }
                SopoLog.e("SOPO API Error $errorCode", exception)
                if(errorCode == ErrorEnum.ALREADY_REGISTERED_USER) return callback.onSignUpError(errorCode)
                callback.onFailure(errorCode)
            }
            is OAuthException ->
            {
                val errorCode = ErrorEnum.getErrorCode(exception.getErrorResponse().code).apply {
                    message = exception.getErrorResponse().message
                }
                SopoLog.e("OAuthException API Error $errorCode", exception)
                if(errorCode == ErrorEnum.OAUTH2_INVALID_GRANT || errorCode == ErrorEnum.OAUTH2_INVALID_TOKEN)
                {
                    return callback.onLoginError(errorCode)
                }
                else if(errorCode == ErrorEnum.OAUTH2_DELETE_TOKEN)
                {

                }

                callback.onAuthError(errorCode)
            }
            is InternalServerException ->
            {
                val errorCode = ErrorEnum.getErrorCode(exception.getErrorResponse().code).apply {
                    message = exception.getErrorResponse().message
                }
                SopoLog.e("InternalServerException API Error $errorCode", exception)
                callback.onInternalServerError(errorCode)
            }
            else ->
            {
                callback.onFailure(ErrorEnum.UNKNOWN_ERROR)
            }
        }
    }
}