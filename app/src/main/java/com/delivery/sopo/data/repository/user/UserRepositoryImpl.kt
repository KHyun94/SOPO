package com.delivery.sopo.data.repository.user

import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.resource.auth.local.AuthDataSource
import com.delivery.sopo.data.resource.auth.remote.AuthRemoteDataSource
import com.delivery.sopo.data.resource.user.local.UserDataSource
import com.delivery.sopo.data.resource.user.remote.UserRemoteDataSource
import com.delivery.sopo.enums.ErrorType
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.models.api.ErrorResponse
import com.delivery.sopo.models.user.ResetAuthCode
import com.delivery.sopo.models.user.ResetPassword
import com.delivery.sopo.util.DateUtil

class UserRepositoryImpl(
        private val authDataSource: AuthDataSource, private val authRemoteDataSource: AuthRemoteDataSource,
        private val userDataSource: UserDataSource, private val userRemoteDataSource: UserRemoteDataSource): UserRepository
{
    override suspend fun login()
    {
        val userName = userDataSource.getUserName()
        val password = userDataSource.getUserPassword()

        val tokenInfo = authRemoteDataSource.issueToken(userName = userName, password = password)

        userDataSource.insertUserAccount(userName, password, StatusConst.ACTIVATE)
        authDataSource.insert(token = tokenInfo)
    }

    override suspend fun login(userName: String, password: String)
    {
        val tokenInfo = authRemoteDataSource.issueToken(userName = userName, password = password)

        userDataSource.insertUserAccount(userName, password, StatusConst.ACTIVATE)
        authDataSource.insert(token = tokenInfo)
    }

    override suspend fun refreshToken(): String
    {
        val userName = userDataSource.getUserName()
        val refreshToken = authDataSource.get(userName = userName).refreshToken
        val tokenInfo = authRemoteDataSource.refreshToken(userName = userName, refreshToken = refreshToken)

        authDataSource.insert(token = tokenInfo)

        return tokenInfo.refreshToken
    }

    override suspend fun fetchUserInfo()
    {
        val userInfo = userRemoteDataSource.fetchUserInfo()
        val nickname = userInfo.nickname ?: throw SOPOApiException(404, ErrorResponse(609, ErrorType.NO_RESOURCE, "조회한 데이터가 존재하지 않습니다.", ""))
        userDataSource.insertUserInfo(nickname, userInfo.personalMessage)
    }

    override suspend fun updateNickname(nickname: String)
    {
        userRemoteDataSource.updateNickname(nickname = nickname)
    }

    override suspend fun updateFCMToken(fcmToken: String)
    {
        userRemoteDataSource.updateFCMToken(fcmToken = fcmToken)
    }

    override suspend fun requestAuthCodeEmail(email: String)
    {
        userRemoteDataSource.requestAuthCodeEmail(email = email)
    }

    override suspend fun requestVerifyAuthToken(authCode: ResetAuthCode)
    {
        userRemoteDataSource.requestVerifyAuthToken(authCode = authCode)
    }

    override suspend fun updatePassword(resetPassword: ResetPassword)
    {
        userRemoteDataSource.updatePassword(resetPassword = resetPassword)
        userDataSource.setUserPassword(resetPassword.password)
    }

    // TODO Table CLEAR
    override suspend fun deleteUser(reason: String)
    {
        userRemoteDataSource.deleteUser(reason = reason)
        userDataSource.removeUserRepo()
    }

    override suspend fun checkExpiredTokenWithInWeek():Boolean{
        val userName = userDataSource.getUserName()
        val currentExpiredDate: String = authDataSource.get(userName).refreshTokenExpiredAt
        return DateUtil.isExpiredDateWithinAWeek(currentExpiredDate)
    }


}