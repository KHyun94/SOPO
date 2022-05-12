package com.delivery.sopo.data.resource.user.local

import com.delivery.sopo.models.PersonalMessage
import com.delivery.sopo.models.dto.OAuthToken

interface UserDataSource
{
    suspend fun getToken(): OAuthToken
    suspend fun insertToken(token: OAuthToken)
    suspend fun updateToken(token: OAuthToken)
    suspend fun deleteToken(token: OAuthToken)

    fun insertUserAccount(userName: String, password: String, status: Int)
    fun insertUserInfo(nickname: String, personalMessage: PersonalMessage)

    fun getNickname() :String
    fun setNickname(nickname : String)

    fun getUserName(): String
    fun setUserId(userId: String)

    fun getUserPassword(): String
    fun setUserPassword(password: String)

    fun getDeviceInfo(): String
    fun setDeviceInfo(info: String)

    fun getRegisterDate(): String
    fun setRegisterDate(regDt: String)

    fun getStatus(): Int
    fun setStatus(status: Int)

    fun getJoinType(): String
    fun setJoinType(joinType: String)

    fun getSNSUId(): String?
    fun setSNSUId(uid: String)

    fun getPersonalStatusType(): Int
    fun setPersonalStatusType(type: Int)

    fun getPersonalStatusMessage(): String
    fun setPersonalStatusMessage(message: String)

    fun getAppPassword(): String
    fun setAppPassword(password: String)

    fun getTopic(): String
    fun setTopic(topic: String)

    fun getDisturbStartTime(): String?
    fun setDisturbStartTime(startTime: String)

    fun getDisturbEndTime(): String?
    fun setDisturbEndTime(startTime: String)

    fun removeUserRepo()

}