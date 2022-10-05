package com.delivery.sopo.data.repositories.user

import com.delivery.sopo.data.resources.auth.local.AuthDataSource
import com.delivery.sopo.data.resources.auth.remote.AuthRemoteDataSource
import com.delivery.sopo.data.resources.user.local.UserDataSource
import com.delivery.sopo.data.resources.user.remote.UserRemoteDataSource
import com.delivery.sopo.enums.ErrorType
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.models.api.Error
import com.delivery.sopo.models.user.ResetAuthCode
import com.delivery.sopo.models.user.ResetPassword
import com.delivery.sopo.util.DateUtil
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor(private val authDataSource: AuthDataSource, private val authRemoteDataSource: AuthRemoteDataSource, private val userDataSource: UserDataSource, private val userRemoteDataSource: UserRemoteDataSource):
        UserRepository
{
    override suspend fun login()
    {
        val username = userDataSource.getUsername()
        val userPassword = userDataSource.getUserPassword()

        val authToken = authRemoteDataSource.issueToken(username = username, password = userPassword)

        userDataSource.insertUserAccount(username = username, userPassword = userPassword)
        authDataSource.insert(token = authToken)
    }

    override suspend fun login(username: String, userPassword: String)
    {
        val authToken = authRemoteDataSource.issueToken(username = username, password = userPassword)
        userDataSource.insertUserAccount(username = username, userPassword = userPassword)
        authDataSource.insert(token = authToken)
    }

    override suspend fun refreshToken(): String
    {
        val refreshToken = authDataSource.getRefreshToken()
        val tokenInfo = authRemoteDataSource.refreshToken(refreshToken = refreshToken)

        authDataSource.insert(token = tokenInfo)

        return tokenInfo.accessToken
    }

    override suspend fun fetchUserInfo()
    {
        val userInfo = userRemoteDataSource.fetchUserInfo()
        val nickname = userInfo.nickname
            ?: throw SOPOApiException(404, Error(609, ErrorType.NO_RESOURCE, "조회한 데이터가 존재하지 않습니다.", "")) //        userDataSource.insertUserInfo(nickname, userInfo.personalMessage)
    }

    override suspend fun updateNickname(nickname: String)
    {
        userRemoteDataSource.updateNickname(nickname = nickname)
    }

    override suspend fun updateFCMToken(fcmToken: String)
    {
        userRemoteDataSource.updateFCMToken(fcmToken = fcmToken)
    }

    override suspend fun requestAuthCodeEmail(email: String): String
    {
        return userRemoteDataSource.requestAuthCodeEmail(email = email)
    }

    override suspend fun requestVerifyAuthToken(authCode: ResetAuthCode)
    {
        return userRemoteDataSource.requestVerifyAuthToken(authCode = authCode)
    }

    override suspend fun updatePassword(resetPassword: ResetPassword)
    {
        userRemoteDataSource.updatePassword(resetPassword = resetPassword)
        userDataSource.setUserPassword(resetPassword.password)
    }

    override suspend fun deleteUser(reason: String)
    {
        userRemoteDataSource.deleteUser(reason = reason) //        userDataSource.clearUserDataBase()
    }

    override suspend fun checkExpiredTokenWithInWeek(): Boolean
    {
        val userName = userDataSource.getUsername()
        val currentExpiredDate: String = authDataSource.getExpireAt()
        return DateUtil.isExpiredDateWithinAWeek(currentExpiredDate)
    }

    override suspend fun getUserDataSource(): UserDataSource
    {
        return userDataSource
    }

}