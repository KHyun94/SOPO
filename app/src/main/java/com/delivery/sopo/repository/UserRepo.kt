package com.delivery.sopo.repository

import com.delivery.sopo.shared.SharedPrefHelper

class UserRepo(private val shared: SharedPrefHelper)
{
    fun getEmail(): String
    {
        return shared.getUserEmail() ?: ""
    }

    fun setEmail(email: String)
    {
        shared.setUserEmail(email = email)
    }

    fun getApiPwd(): String
    {
        return shared.getPrivateApiPwd() ?: ""
    }

    fun setApiPwd(pwd: String)
    {
        return shared.setPrivateApiPwd(pwd)
    }

    fun getDeviceInfo(): String
    {
        return shared.getDeviceInfo() ?: ""
    }

    fun setDeviceInfo(info: String)
    {
        return shared.setDeviceInfo(info)
    }

    fun getRegisterDate(): String
    {
        return shared.getRegisterDate() ?: ""
    }

    fun setRegisterDate(regDt: String)
    {
        shared.setRegisterDate(regDt)
    }

    fun getStatus(): Int
    {
        return shared.getStatus() ?: 0
    }

    fun setStatus(status: Int)
    {
        shared.setStatus(status)
    }

    fun getJoinType(): String
    {
        return shared.getJoinType() ?: ""
    }

    fun setJoinType(joinType: String)
    {
        shared.setJoinType(joinType)
    }

    fun getSNSUId(): String?
    {
        return shared.getSNSUId()
    }

    fun setSNSUId(uid: String)
    {
        shared.setSNSUId(uid)
    }

    fun getAppPassword(): String{
        return shared.getAppPassword() ?: ""
    }

    fun setAppPassword(password: String){
        shared.setAppPassword(password)
    }


    fun removeUserRepo()
    {
        setEmail("")
        setApiPwd("")
        setJoinType("")
        setRegisterDate("")
        setStatus(0)
        setDeviceInfo("")
    }

}