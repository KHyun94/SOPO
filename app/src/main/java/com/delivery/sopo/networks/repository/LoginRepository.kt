package com.delivery.sopo.networks.repository

import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.JoinTypeConst
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.SuccessResult
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.networks.datasource.CustomTokenCallback
import com.delivery.sopo.networks.datasource.LoginDataSource
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.CodeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class LoginRepository: LoginDataSource
{
    override suspend fun requestCustomToken(email: String, deviceInfo: String, joinType: String, kakaoUid: String, callback: CustomTokenCallback) =
        withContext(Dispatchers.Main) {
            when (val result = UserCall.requestCustomToken(email = email, deviceInfo = SOPOApp.deviceInfo, joinType = JoinTypeConst.KAKAO, userId = kakaoUid))
            {
                is NetworkResult.Success ->
                {
                    val apiResult = result.data

                    when (val code = CodeUtil.getCode(apiResult.code))
                    {
                        ResponseCode.SUCCESS ->
                        {
                            val customToken = apiResult.data ?: ""

                            callback.invoke(SuccessResult(code, code.MSG, customToken), null)
                        }
                        else ->
                        {
                            callback.invoke(null, ErrorResult(code = code, errorMsg = CodeUtil.getMsg(apiResult.code), errorType = ErrorResult.ERROR_TYPE_DIALOG, e = null))
                        }
                    }
                }
                is NetworkResult.Error ->
                {
                    val exception = result.exception as APIException
                    val errorCode = CodeUtil.getCode(exception.data()?.code)
                    callback.invoke(null, ErrorResult<String?>(code = errorCode, errorMsg = errorCode.MSG, errorType = ErrorResult.ERROR_TYPE_DIALOG, e = exception))
                }
            }

        }
}