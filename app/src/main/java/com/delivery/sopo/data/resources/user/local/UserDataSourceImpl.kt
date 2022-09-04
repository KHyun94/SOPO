package com.delivery.sopo.data.resources.user.local

import com.delivery.sopo.data.database.datastore.DataStoreKey.APP_PASSWORD
import com.delivery.sopo.data.database.datastore.DataStoreKey.DEVICE_INFO
import com.delivery.sopo.data.database.datastore.DataStoreKey.DISTURB_END_TIME
import com.delivery.sopo.data.database.datastore.DataStoreKey.DISTURB_START_TIME
import com.delivery.sopo.data.database.datastore.DataStoreKey.FCM_TOPIC
import com.delivery.sopo.data.database.datastore.DataStoreKey.JOIN_TYPE
import com.delivery.sopo.data.database.datastore.DataStoreKey.PERSONAL_STATUS_MESSAGE
import com.delivery.sopo.data.database.datastore.DataStoreKey.PERSONAL_STATUS_TYPE
import com.delivery.sopo.data.database.datastore.DataStoreKey.PUSH_ALARM_TYPE
import com.delivery.sopo.data.database.datastore.DataStoreKey.REGISTER_DATE
import com.delivery.sopo.data.database.datastore.DataStoreKey.SNS_UID
import com.delivery.sopo.data.database.datastore.DataStoreKey.STATUS
import com.delivery.sopo.data.database.datastore.DataStoreKey.USER_NAME
import com.delivery.sopo.data.database.datastore.DataStoreKey.USER_NICKNAME
import com.delivery.sopo.data.database.datastore.DataStoreKey.USER_PASSWORD
import com.delivery.sopo.data.database.datastore.DataStoreKey.USER_TOKEN
import com.delivery.sopo.data.database.datastore.DataStoreManager
import com.delivery.sopo.data.database.shared.UserSharedPrefHelper
import com.delivery.sopo.enums.SettingEnum
import javax.inject.Inject

class UserDataSourceImpl @Inject constructor(private val dataStoreManager: DataStoreManager): UserDataSource
{
    override suspend fun getNickname(): String
    {
        return dataStoreManager.readValue(USER_NICKNAME) ?: ""
    }

    override suspend fun setNickname(nickname: String)
    {
        dataStoreManager.storeValue(USER_NICKNAME, nickname)
    }

    override suspend fun getUsername(): String
    {
        return dataStoreManager.readValue(USER_NAME) ?: ""
    }

    override suspend fun setUsername(username: String)
    {
        dataStoreManager.storeValue(USER_NAME, username)
    }

    override suspend fun getUserToken(): String
    {
        return dataStoreManager.readValue(USER_TOKEN)?: ""
    }

    override suspend fun setUserToken(userToken: String)
    {
        dataStoreManager.storeValue(USER_TOKEN, userToken)
    }

    override suspend fun getUserPassword(): String
    {
        return dataStoreManager.readValue(USER_PASSWORD)?: ""
    }

    override suspend fun setUserPassword(password: String)
    {
        return dataStoreManager.storeValue(USER_PASSWORD, password)
    }

    override suspend fun getDeviceInfo(): String
    {
        return dataStoreManager.readValue(DEVICE_INFO)?: ""
    }

    override suspend fun setDeviceInfo(info: String)
    {
        return dataStoreManager.storeValue(DEVICE_INFO, info)
    }

    override suspend fun getRegisterDate(): String
    {
        return dataStoreManager.readValue(REGISTER_DATE)?: ""
    }

    override suspend fun setRegisterDate(regDt: String)
    {
        dataStoreManager.storeValue(REGISTER_DATE, regDt)
    }

    override suspend fun getStatus(): Int
    {
        return dataStoreManager.readValue(STATUS)?: 0
    }

    override suspend fun setStatus(status: Int)
    {
        dataStoreManager.storeValue(STATUS, status)
    }

    override suspend fun getJoinType(): String
    {
        return dataStoreManager.readValue(JOIN_TYPE)?: ""
    }

    override suspend fun setJoinType(joinType: String)
    {
        dataStoreManager.storeValue(JOIN_TYPE, joinType)
    }

    override suspend fun getSNSUId(): String
    {
        return dataStoreManager.readValue(SNS_UID)?: ""
    }

    override suspend fun setSNSUId(uid: String)
    {
        dataStoreManager.storeValue(SNS_UID, uid)
    }

    override suspend fun getPersonalStatusType(): Int
    {
        return dataStoreManager.readValue(PERSONAL_STATUS_TYPE)?: 0
    }

    override suspend fun setPersonalStatusType(type: Int)
    {
        dataStoreManager.storeValue(PERSONAL_STATUS_TYPE, type)
    }

    override suspend fun getPersonalStatusMessage(): String
    {
        return dataStoreManager.readValue(PERSONAL_STATUS_MESSAGE)?: ""
    }

    override suspend fun setPersonalStatusMessage(message: String)
    {
        dataStoreManager.storeValue(PERSONAL_STATUS_MESSAGE, message)
    }

    override suspend fun getAppPassword(): String
    {
        return dataStoreManager.readValue(APP_PASSWORD)?: ""
    }

    override suspend fun setAppPassword(password: String)
    {
        dataStoreManager.storeValue(APP_PASSWORD, password)
    }

    override suspend fun getTopic(): String
    {
        return dataStoreManager.readValue(FCM_TOPIC)?: ""
    }

    override suspend fun setTopic(topic: String)
    {
        dataStoreManager.storeValue(FCM_TOPIC, topic)
    }

    override suspend fun getDisturbStartTime(): String
    {
        return dataStoreManager.readValue(DISTURB_START_TIME)?: ""
    }

    override suspend fun setDisturbStartTime(startTime: String)
    {
        dataStoreManager.storeValue(DISTURB_START_TIME, startTime)
    }

    override suspend fun getDisturbEndTime(): String
    {
        return dataStoreManager.readValue(DISTURB_END_TIME)?: ""
    }

    override suspend fun setDisturbEndTime(endTime: String)
    {
        dataStoreManager.storeValue(DISTURB_END_TIME, endTime)
    }

    suspend fun getPushAlarmType(): String
    {
        return dataStoreManager.readValue(PUSH_ALARM_TYPE)?:""
    }

    suspend fun setPushAlarmType(pushAlarmType: SettingEnum.PushAlarmType)
    {
        dataStoreManager.storeValue(PUSH_ALARM_TYPE, pushAlarmType.name)
    }
}