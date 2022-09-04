package com.delivery.sopo.data.resources.user.local

import com.delivery.sopo.models.PersonalMessage
import kotlinx.coroutines.flow.Flow

interface UserDataSource
{
    suspend fun getNickname() : String
    suspend fun setNickname(nickname : String)

    suspend fun getUsername(): String
    suspend fun setUsername(username: String)

    suspend fun getUserToken(): String
    suspend fun setUserToken(userToken: String)

    suspend fun getUserPassword(): String
    suspend fun setUserPassword(password: String)

    suspend fun getDeviceInfo(): String
    suspend fun setDeviceInfo(info: String)

    suspend fun getRegisterDate(): String
    suspend fun setRegisterDate(regDt: String)

    suspend fun getStatus(): Int
    suspend fun setStatus(status: Int)

    suspend fun getJoinType(): String
    suspend fun setJoinType(joinType: String)

    suspend fun getSNSUId(): String
    suspend fun setSNSUId(uid: String)

    suspend fun getPersonalStatusType(): Int
    suspend fun setPersonalStatusType(type: Int)

    suspend fun getPersonalStatusMessage(): String
    suspend fun setPersonalStatusMessage(message: String)

    suspend fun getAppPassword(): String
    suspend fun setAppPassword(password: String)

    suspend fun getTopic(): String
    suspend fun setTopic(topic: String)

    suspend fun getDisturbStartTime(): String
    suspend fun setDisturbStartTime(startTime: String)

    suspend fun getDisturbEndTime(): String
    suspend fun setDisturbEndTime(endTime: String)

//    fun clearUserDataBase()

}