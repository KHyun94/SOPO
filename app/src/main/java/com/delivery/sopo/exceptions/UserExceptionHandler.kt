package com.delivery.sopo.exceptions

import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.usecase.LogoutUseCase
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import org.koin.core.KoinComponent
import org.koin.core.inject
import kotlin.coroutines.CoroutineContext

class UserExceptionHandler(private val dispatcher: CoroutineDispatcher, private val callback: OnSOPOErrorCallback):
        CoroutineExceptionHandler, KoinComponent
{
    override val key: CoroutineContext.Key<*> = dispatcher.key

    private val logoutUseCase: LogoutUseCase by inject()

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
                if(errorCode == ErrorEnum.ALREADY_REGISTERED_USER) return callback.onErrorAlreadyRegisteredUser(errorCode)
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
                    logoutUseCase.invoke()
                    return callback.onDuplicateError(errorCode)
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