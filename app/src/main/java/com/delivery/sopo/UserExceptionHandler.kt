package com.delivery.sopo

import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.OAuthException
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlin.coroutines.CoroutineContext

class UserExceptionHandler(
        private val dispatcher: CoroutineDispatcher,
        private val callback: OnSOPOErrorCallback): CoroutineExceptionHandler
{
    override val key: CoroutineContext.Key<*> = dispatcher.key

    override fun handleException(context: CoroutineContext, exception: Throwable)
    {
        SopoLog.e("UseExceptionHandler -> ${exception.toString()}")

        when(exception)
        {
            is SOPOApiException ->
            {
                val errorCode = ErrorEnum.getErrorCode(exception.getErrorResponse().code)
                SopoLog.e("SOPO API Error $errorCode", exception)
                if(errorCode == ErrorEnum.ALREADY_REGISTERED_USER) return callback.onSignUpError(errorCode)
                callback.onFailure(errorCode)
            }
            is OAuthException ->
            {
                val errorCode = ErrorEnum.getErrorCode(exception.getErrorResponse().code)
                SopoLog.e("OAuthException API Error $errorCode", exception)
                if(errorCode == ErrorEnum.OAUTH2_INVALID_GRANT || errorCode == ErrorEnum.OAUTH2_INVALID_TOKEN)
                {
                    return callback.onLoginError(errorCode)
                }
                callback.onAuthError(errorCode)
            }
            is InternalServerException ->
            {
                val errorCode = ErrorEnum.getErrorCode(exception.getErrorResponse().code)
                SopoLog.e("InternalServerException API Error $errorCode", exception)
                callback.onInternalServerError(errorCode)
            }
            else ->
            {

            }
        }
    }
}