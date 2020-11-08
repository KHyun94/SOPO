package com.delivery.sopo.repository.interfaces

interface UserRepository
{

    fun getUserNickname() :String
    fun setUserNickname(nickname : String)
    fun getEmail(): String
    fun setEmail(email: String)
    fun getApiPwd(): String
    fun setApiPwd(pwd: String)
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
    fun getAppPassword(): String
    fun setAppPassword(password: String)
    fun removeUserRepo()
}