package com.delivery.sopo.database.shared

import android.content.Context

class SharedPrefHelper(private val sharedPref: SharedPref, private val context: Context)
{

    private val USER_EMAIL = "USER_EMAIL"
    private val DEVICE_INFO = "USER_DEVICE_INFO"
    private val PRIVATE_API_PWD = "USER_API_PWD"
    private val JOIN_TYPE = "JOIN_TYPE"
    private val USER_NICKNAME = "USER_NICKNAME"
    private val REGISTER_DATE = "REGISTER_DATE"
    private val STATUS = "STATUS"
    private val SNS_UID = "SNS_UID"
    private val APP_PASSWORD = "APP_PASSWORD"

    fun getUserNickname(): String?
    {
        return sharedPref.getString(USER_NICKNAME, "")
    }

    fun setUserNickname(userId: String)
    {
        sharedPref.setString(USER_NICKNAME, userId)
    }

    fun getUserEmail(): String?
    {
        return sharedPref.getString(USER_EMAIL, "")
    }

    fun setUserEmail(email: String)
    {
        sharedPref.setString(USER_EMAIL, email)
    }

    fun getDeviceInfo(): String?
    {
        return sharedPref.getString(DEVICE_INFO, "")
    }

    fun setDeviceInfo(deviceInfo: String)
    {
        sharedPref.setString(DEVICE_INFO, deviceInfo)
    }

    fun getJoinType(): String?
    {
        return sharedPref.getString(JOIN_TYPE, "")
    }

    fun setJoinType(deviceInfo: String)
    {
        sharedPref.setString(JOIN_TYPE, deviceInfo)
    }

    fun getPrivateApiPwd(): String?
    {
        return sharedPref.getString(PRIVATE_API_PWD, "")
    }

    fun setPrivateApiPwd(pwd: String)
    {
        sharedPref.setString(PRIVATE_API_PWD, pwd)
    }

    fun getRegisterDate(): String?
    {
        return sharedPref.getString(REGISTER_DATE, "")
    }

    fun setRegisterDate(regDt: String)
    {
        sharedPref.setString(REGISTER_DATE, regDt)
    }

    fun getStatus(): Int?
    {
        return sharedPref.getInt(STATUS, 0)
    }

    fun setStatus(status: Int)
    {
        sharedPref.setInt(STATUS, status)
    }

    fun getSNSUId(): String?
    {
        return sharedPref.getString(SNS_UID, "0000000000")
    }

    fun setSNSUId(uid: String)
    {
        sharedPref.setString(SNS_UID, uid)
    }

    fun getAppPassword(): String?
    {
        return sharedPref.getString(APP_PASSWORD, "")
    }

    fun setAppPassword(password: String)
    {
        sharedPref.setString(APP_PASSWORD, password)
    }
}