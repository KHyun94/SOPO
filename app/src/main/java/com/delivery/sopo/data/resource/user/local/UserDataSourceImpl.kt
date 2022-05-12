package com.delivery.sopo.data.resource.user.local

import com.delivery.sopo.data.database.room.dao.OAuthDao
import com.delivery.sopo.data.repository.local.user.UserSharedPrefHelper
import com.delivery.sopo.enums.SettingEnum
import com.delivery.sopo.models.PersonalMessage
import com.delivery.sopo.models.dto.OAuthToken
import com.delivery.sopo.models.mapper.OAuthMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserDataSourceImpl(private val userShared: UserSharedPrefHelper, private val oAuthDao: OAuthDao):
        UserDataSource
{
    override suspend fun getToken() = withContext(Dispatchers.Default) {
        oAuthDao.get(userId = getUserName()) ?: throw NullPointerException("OAuth Token 정보가 없습니다.")
    }.run(OAuthMapper::entityToObject)

    override suspend fun insertToken(token: OAuthToken) = withContext(Dispatchers.Default) {
        val entity = OAuthMapper.objectToEntity(oAuth = token)
        oAuthDao.insert(entity)
    }

    override suspend fun updateToken(token: OAuthToken)
    {
        val entity = OAuthMapper.objectToEntity(oAuth = token)
        oAuthDao.update(entity)
    }

    override suspend fun deleteToken(token: OAuthToken)
    {
        val entity = OAuthMapper.objectToEntity(oAuth = token)
        oAuthDao.delete(entity)
    }

    override fun insertUserAccount(userName: String, password: String, status: Int){
        setUserId(userName)
        setUserPassword(password)
        setStatus(status)
    }

    override fun insertUserInfo(nickname: String, personalMessage: PersonalMessage)
    {
        setNickname(nickname = nickname)
        setPersonalStatusType(type = personalMessage.type)
        setPersonalStatusMessage(message = personalMessage.message)
    }

    override fun getNickname(): String
    {
        return userShared.getNickname() ?: ""
    }

    override fun setNickname(nickname: String)
    {
        userShared.setNickname(nickname)
    }

    override fun getUserName(): String
    {
        return userShared.getUserId() ?: ""
    }

    override fun setUserId(userId: String)
    {
        userShared.setUserId(userId = userId)
    }

    override fun getUserPassword(): String
    {
        return userShared.getUserPassword() ?: ""
    }

    override fun setUserPassword(password: String)
    {
        return userShared.setUserPassword(password)
    }

    override fun getDeviceInfo(): String
    {
        return userShared.getDeviceInfo() ?: ""
    }

    override fun setDeviceInfo(info: String)
    {
        return userShared.setDeviceInfo(info)
    }

    override fun getRegisterDate(): String
    {
        return userShared.getRegisterDate() ?: ""
    }

    override fun setRegisterDate(regDt: String)
    {
        userShared.setRegisterDate(regDt)
    }

    override fun getStatus(): Int
    {
        return userShared.getStatus() ?: 0
    }

    override fun setStatus(status: Int)
    {
        userShared.setStatus(status)
    }

    override fun getJoinType(): String
    {
        return userShared.getJoinType() ?: ""
    }

    override fun setJoinType(joinType: String)
    {
        userShared.setJoinType(joinType)
    }

    override fun getSNSUId(): String?
    {
        return userShared.getSNSUId()
    }

    override fun setSNSUId(uid: String)
    {
        userShared.setSNSUId(uid)
    }

    override fun getPersonalStatusType(): Int
    {
        return userShared.getPersonalStatusType()
    }

    override fun setPersonalStatusType(type: Int)
    {
        userShared.setPersonalStatusType(type)
    }

    override fun getPersonalStatusMessage(): String
    {
        return userShared.getPersonalStatusMessage()
    }

    override fun setPersonalStatusMessage(message: String)
    {
        userShared.setPersonalStatusMessage(message)
    }

    override fun getAppPassword(): String
    {
        return userShared.getAppPassword() ?: ""
    }

    override fun setAppPassword(password: String)
    {
        userShared.setAppPassword(password)
    }

    override fun getTopic() = userShared.getTopic() ?: ""

    override fun setTopic(topic: String)
    {
        userShared.setTopic(topic)
    }

    override fun getDisturbStartTime() = userShared.getDisturbStartTime()

    override fun setDisturbStartTime(startTime: String)
    {
        userShared.setDisturbStartTime(startTime)
    }

    override fun getDisturbEndTime() = userShared.getDisturbEndTime()

    override fun setDisturbEndTime(endTime: String)
    {
        userShared.setDisturbEndTime(endTime)
    }

    fun getPushAlarmType(): SettingEnum.PushAlarmType
    {
        return userShared.getPushAlarmType()
    }

    fun setPushAlarmType(pushAlarmType: SettingEnum.PushAlarmType)
    {
        userShared.setPushAlarmType(pushAlarmType)
    }

    override fun removeUserRepo()
    {
        setUserId("")
        setUserPassword("")
        setJoinType("")
        setRegisterDate("")
        setStatus(0)
        setDeviceInfo("")
        setPushAlarmType(SettingEnum.PushAlarmType.ALWAYS)
    }


}