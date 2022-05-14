package com.delivery.sopo.data.repository.remote.user

import com.delivery.sopo.data.networks.NetworkManager
import com.delivery.sopo.data.networks.serivces.UserService
import com.delivery.sopo.enums.ErrorType
import com.delivery.sopo.enums.NetworkEnum
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.extensions.wrapBodyAliasToHashMap
import com.delivery.sopo.models.api.ErrorResponse
import com.delivery.sopo.models.user.ResetAuthCode
import com.delivery.sopo.models.user.ResetPassword
import com.delivery.sopo.services.network_handler.BaseService
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent

class UserRemoteRepository: KoinComponent, BaseService()
{
    suspend fun requestSendTokenToEmail(email: String): String
    {
        val requestEmailForAuth = NetworkManager.setLoginMethod(NetworkEnum.PRIVATE_LOGIN, UserService::class.java).requestAuthCodeEmail(email = email)
        val result = apiCall { requestEmailForAuth }
        return result.data?.data?: throw SOPOApiException(404, ErrorResponse(404, ErrorType.NO_RESOURCE, "조회한 데이터가 존재하지 않습니다.", ""))
    }

    suspend fun requestVerifyAuthToken(resetAuthCode: ResetAuthCode)
    {
        val requestEmailForAuth = NetworkManager.setLoginMethod(NetworkEnum.PRIVATE_LOGIN, UserService::class.java).requestVerifyAuthToken(authCode = resetAuthCode)
        apiCall { requestEmailForAuth }
    }

    suspend fun requestResetPassword(resetPassword: ResetPassword)
    {
        val requestEmailForAuth = NetworkManager.setLoginMethod(NetworkEnum.PRIVATE_LOGIN, UserService::class.java).updatePassword(resetPassword = resetPassword)
        apiCall { requestEmailForAuth }
    }
}