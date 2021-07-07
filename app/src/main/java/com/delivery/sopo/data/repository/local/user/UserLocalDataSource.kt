package com.delivery.sopo.data.repository.local.user

interface UserLocalDataSource
{
    fun getNickname() :String
    fun setNickname(nickname : String)

    fun getUserId(): String
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