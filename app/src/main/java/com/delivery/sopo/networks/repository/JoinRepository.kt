package com.delivery.sopo.networks.repository

import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.extensions.match
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.SuccessResult
import com.delivery.sopo.networks.call.JoinCall
import com.delivery.sopo.networks.datasource.DuplicateCallback
import com.delivery.sopo.networks.datasource.JoinCallback
import com.delivery.sopo.networks.datasource.JoinDataSource
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.CodeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JoinRepository : JoinDataSource
{
    override suspend fun requestJoinBySelf(
        email: String,
        password: String,
        deviceInfo: String,
        firebaseUid: String,
        callback: JoinCallback
    ) = withContext(Dispatchers.IO) {
        when (val result = JoinCall.requestJoinBySelf(email, password, deviceInfo, firebaseUid))
        {
            is NetworkResult.Success ->
            {
                val apiResult = result.data
                val code = CodeUtil.getCode(apiResult.code)
                callback.invoke(SuccessResult(code, code.MSG, null), null)
            }
            is NetworkResult.Error ->
            {
                val exception = result.exception as APIException
                val errorCode = exception.match
                callback.invoke(
                    null, ErrorResult(
                        errorCode, errorCode.MSG, ErrorResult.ERROR_TYPE_DIALOG, null, exception
                    )
                )
            }
        }
    }

    override suspend fun requestJoinByKakao(
        email: String,
        password: String,
        deviceInfo: String,
        kakaoUid : String,
        firebaseUid: String,
        callback: JoinCallback
    )= withContext(Dispatchers.IO) {
        when (val result = JoinCall.requestJoinByKakao(email, password, deviceInfo, kakaoUid, firebaseUid))
        {
            is NetworkResult.Success ->
            {
                val apiResult = result.data
                val code = CodeUtil.getCode(apiResult.code)
                callback.invoke(SuccessResult(code, code.MSG, null), null)
            }
            is NetworkResult.Error ->
            {
                val exception = result.exception as APIException
                val errorCode = exception.match
                callback.invoke(
                    null, ErrorResult(
                        errorCode, errorCode.MSG, ErrorResult.ERROR_TYPE_DIALOG, null, exception
                    )
                )
            }
        }
    }

    override suspend fun requestDuplicatedEmail(email: String, callback: DuplicateCallback) =
        withContext(Dispatchers.Main) {
            when (val result = JoinCall.requestDuplicatedEmail(email))
            {
                is NetworkResult.Success ->
                {
                    val apiResult = result.data
                    val code = CodeUtil.getCode(apiResult.code)

                    callback.invoke(SuccessResult(code, code.MSG, apiResult.data), null)
                }
                is NetworkResult.Error ->
                {
                    val exception = result.exception as APIException
                    val errorCode = exception.responseCode

                    callback.invoke(null, ErrorResult(errorCode, errorCode.MSG, ErrorResult.ERROR_TYPE_NON, false, exception))
                }
            }
        }
}


/**
 *  when(val result = JoinCall.requestJoinBySelf(email, password, deviceInfo, firebaseUid))
{
is Result.Success ->
{
val apiResult = result.data

when(val code = CodeUtil.getCode(apiResult.code))
{
ResponseCodeEnum.SUCCESS ->
{
// 성공하면 인증 메일 전송
}
else ->
{
// 200 이외 나올게 있나?
}
}
}
is Result.Error ->
{
val exception = result.exception as APIException
val errorCode = exception.matchEnum
}
}
}
 */