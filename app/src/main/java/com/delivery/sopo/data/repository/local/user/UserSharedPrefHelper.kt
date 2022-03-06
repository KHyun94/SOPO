package com.delivery.sopo.data.repository.local.user

import android.content.Context
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.data.database.shared.SharedPref
import com.delivery.sopo.enums.SettingEnum
import com.delivery.sopo.util.CodeUtil

class UserSharedPrefHelper(private val sharedPref: SharedPref, private val context: Context)
{

    fun getNickname(): String?
    {
        return sharedPref.getString(InfoConst.USER_NICKNAME, "")
    }

    fun setNickname(userId: String)
    {
        sharedPref.setString(InfoConst.USER_NICKNAME, userId)
    }

    fun getUserId(): String?
    {
        return sharedPref.getString(InfoConst.USER_ID, "")
    }

    fun setUserId(userId: String)
    {
        sharedPref.setString(InfoConst.USER_ID, userId)
    }

    fun getDeviceInfo(): String?
    {
        return sharedPref.getString(InfoConst.DEVICE_INFO, "")
    }

    fun setDeviceInfo(deviceInfo: String)
    {
        sharedPref.setString(InfoConst.DEVICE_INFO, deviceInfo)
    }

    fun getJoinType(): String?
    {
        return sharedPref.getString(InfoConst.JOIN_TYPE, "")
    }

    fun setJoinType(deviceInfo: String)
    {
        sharedPref.setString(InfoConst.JOIN_TYPE, deviceInfo)
    }

    fun getUserPassword(): String?
    {
        return sharedPref.getString(InfoConst.USER_PASSWORD, "")
    }

    fun setUserPassword(password: String)
    {
        sharedPref.setString(InfoConst.USER_PASSWORD, password)
    }

    fun getRegisterDate(): String?
    {
        return sharedPref.getString(InfoConst.REGISTER_DATE, "")
    }

    fun setRegisterDate(regDt: String)
    {
        sharedPref.setString(InfoConst.REGISTER_DATE, regDt)
    }

    fun getStatus(): Int?
    {
        return sharedPref.getInt(InfoConst.STATUS, 0)
    }

    fun setStatus(status: Int)
    {
        sharedPref.setInt(InfoConst.STATUS, status)
    }

    fun getSNSUId(): String?
    {
        return sharedPref.getString(InfoConst.SNS_UID, "0000000000")
    }

    fun setSNSUId(uid: String)
    {
        sharedPref.setString(InfoConst.SNS_UID, uid)
    }

    fun getPersonalStatusType(): Int{
        return sharedPref.getInt(InfoConst.PERSONAL_STATUS_TYPE, 0)
    }

    fun setPersonalStatusType(type: Int){
        sharedPref.setInt(InfoConst.PERSONAL_STATUS_TYPE, type)
    }

    fun getPersonalStatusMessage(): String{
        return sharedPref.getString(InfoConst.PERSONAL_STATUS_MESSAGE, "")?:""
    }
    fun setPersonalStatusMessage(message: String){
        sharedPref.setString(InfoConst.PERSONAL_STATUS_MESSAGE, message)
    }

    /**
     * FCM Topic
     */
    fun getTopic(): String?
    {
        return sharedPref.getString(InfoConst.FCM_TOPIC, "")
    }

    fun setTopic(topic: String)
    {
        sharedPref.setString(InfoConst.FCM_TOPIC, topic)
    }

    fun getDisturbStartTime() = sharedPref.getString(InfoConst.DISTURB_START_TIME, "")

    fun setDisturbStartTime(startTime: String)
    {
        sharedPref.setString(InfoConst.DISTURB_START_TIME, startTime)
    }

    fun getDisturbEndTime() = sharedPref.getString(InfoConst.DISTURB_END_TIME, "")

    fun setDisturbEndTime(endTime: String)
    {
        sharedPref.setString(InfoConst.DISTURB_END_TIME, endTime)
    }

    fun getAppPassword(): String?
    {
        return sharedPref.getString(InfoConst.APP_PASSWORD, "")
    }

    fun setAppPassword(password: String)
    {
        sharedPref.setString(InfoConst.APP_PASSWORD, password)
    }

    fun getPushAlarmType(): SettingEnum.PushAlarmType
    {
        val pushAlarmType = sharedPref.getString(InfoConst.PUSH_ALARM_TYPE, SettingEnum.PushAlarmType.ALWAYS.name)
            ?: return SettingEnum.PushAlarmType.ALWAYS

        return CodeUtil.getEnumValueOfName<SettingEnum.PushAlarmType>(pushAlarmType)
    }

    fun setPushAlarmType(pushAlarmType: SettingEnum.PushAlarmType)
    {
        sharedPref.setString(InfoConst.PUSH_ALARM_TYPE, pushAlarmType.name)
    }
}