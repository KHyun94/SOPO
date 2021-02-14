package com.delivery.sopo.repository.impl

import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.database.shared.SharedPrefHelper
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.repository.interfaces.UserRepository
import com.delivery.sopo.util.SopoLog
import org.koin.core.KoinComponent
import org.koin.core.inject

//todo kh impl로 수정할
class UserRepoImpl(private val shared: SharedPrefHelper) : UserRepository, KoinComponent
{
    private val userRepoImpl : UserRepoImpl by inject()

    override fun getUserNickname(): String
    {
        return shared.getUserNickname() ?: ""
    }

    override fun setUserNickname(nickname: String)
    {
        shared.setUserNickname(nickname)
    }

    override fun getEmail(): String
    {
        return shared.getUserEmail() ?: ""
    }

    override fun setEmail(email: String)
    {
        shared.setUserEmail(email = email)
    }

    override fun getApiPwd(): String
    {
        return shared.getPrivateApiPwd() ?: ""
    }

    override fun setApiPwd(pwd: String)
    {
        return shared.setPrivateApiPwd(pwd)
    }

    override fun getDeviceInfo(): String
    {
        return shared.getDeviceInfo() ?: ""
    }

    override fun setDeviceInfo(info: String)
    {
        return shared.setDeviceInfo(info)
    }

    override fun getRegisterDate(): String
    {
        return shared.getRegisterDate() ?: ""
    }

    override fun setRegisterDate(regDt: String)
    {
        shared.setRegisterDate(regDt)
    }

    override fun getStatus(): Int
    {
        return shared.getStatus() ?: 0
    }

    override fun setStatus(status: Int)
    {
        shared.setStatus(status)
    }

    override fun getJoinType(): String
    {
        return shared.getJoinType() ?: ""
    }

    override fun setJoinType(joinType: String)
    {
        shared.setJoinType(joinType)
    }

    override fun getSNSUId(): String?
    {
        return shared.getSNSUId()
    }

    override fun setSNSUId(uid: String)
    {
        shared.setSNSUId(uid)
    }

    override fun getAppPassword(): String
    {
        return shared.getAppPassword() ?: ""
    }

    override fun setAppPassword(password: String)
    {
        shared.setAppPassword(password)
    }

    override fun getTopic() = shared.getTopic()?:""

    override fun setTopic(topic : String)
    {
       shared.setTopic(topic)
    }


    override fun removeUserRepo()
    {
        setEmail("")
        setApiPwd("")
        setJoinType("")
        setRegisterDate("")
        setStatus(0)
        setDeviceInfo("")

        FirebaseRepository.unsubscribedToTopicInFCM { successResult, errorResult ->
            if(successResult != null)
            {
                setTopic("")
            }
        }

    }

}