package com.delivery.sopo.viewmodels.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.models.mapper.OAuthMapper
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignUpCompleteViewModel(
        private val userLocalRepo: UserLocalRepository,
        private val userRemoteRepo:UserRemoteRepository,
        private val oAuthRepo: OAuthLocalRepository): ViewModel()
{
    val email = MutableLiveData<String>().also {
        it.postValue(userLocalRepo.getUserId())
    }

    private var _result = MutableLiveData<ResponseResult<*>>()
    val result: LiveData<ResponseResult<*>>
        get() = _result

    val navigator = MutableLiveData<String>()

    val isProgress = MutableLiveData<Boolean?>()

    fun onCompleteClicked()
    {
        login(userLocalRepo.getUserId(), userLocalRepo.getUserPassword())
    }

    fun login(email: String, password: String)
    {
        SopoLog.d("""
            login() call
            email >>> $email
            password >>> $password
        """.trimIndent())

        CoroutineScope(Dispatchers.Main).launch {

            val oAuthRes = userRemoteRepo.requestLogin(email, password)

            SopoLog.d("""
                OAuth Login >>> 
                ${oAuthRes.result}
                ${oAuthRes.message}
                ${oAuthRes.data}
            """.trimIndent())

            if(!oAuthRes.result)
            {
                _result.postValue(oAuthRes)
                return@launch
            }

            if(oAuthRes.data == null)
            {
                return@launch _result.postValue(ResponseResult(false, null, null, "로그인 실패, 다시 시도해주세요.", DisplayEnum.DIALOG))

            }
            val oAuthDTO = oAuthRes.data

            userLocalRepo.run {
                setUserId(email)
                setUserPassword(password)
                setStatus(1)
            }

            SOPOApp.oAuth = oAuthDTO

            withContext(Dispatchers.Default){
                oAuthRepo.insert(oAuthDTO)
            }

            val infoRes = userRemoteRepo.getUserInfo()

            if(!infoRes.result)
            {
                SopoLog.e("Nickname is error")
                _result.postValue(infoRes)
                return@launch
            }

            if(infoRes.data?.nickname == null || infoRes.data.nickname == "")
            {
                SopoLog.e("Nickname is empty, so go to update nickname")
                navigator.postValue(NavigatorConst.TO_UPDATE_NICKNAME)
                return@launch
            }

            SopoLog.e("Nickname is ${infoRes.data.nickname}, so go to main")

            userLocalRepo.setNickname(infoRes.data.nickname!!)

            navigator.postValue(NavigatorConst.TO_MAIN)
        }
    }
}