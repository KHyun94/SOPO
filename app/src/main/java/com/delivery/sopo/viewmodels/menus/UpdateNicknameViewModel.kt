package com.delivery.sopo.viewmodels.menus

import android.os.Handler
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateNicknameViewModel(private val userLocalRepo: UserLocalRepository, private val userRemoteRepo:UserRemoteRepository): ViewModel()
{
    val currentNickname = MutableLiveData<String>().apply {
        postValue(userLocalRepo.getNickname())
    }

    val nickname = MutableLiveData<String>()
    val validates = mutableMapOf<InfoEnum, Boolean>()

    private var _validateError = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val validateError: LiveData<Pair<InfoEnum, Boolean>>
        get() = _validateError

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: MutableLiveData<Triple<View, Boolean, InfoEnum>>
        get() = _focus

    val focusChangeCallback: FocusChangeCallback = FocusChangeCallback@{ v, hasFocus, type ->
        SopoLog.i("${type.NAME} >>> $hasFocus")
        Handler().postDelayed(Runnable { _focus.value = (Triple(v, hasFocus, type)) }, 50)
    }

    // 유효성 및 통신 등의 결과 객체
    private var _result = MutableLiveData<ResponseResult<*>>()
    val result: LiveData<ResponseResult<*>>
        get() = _result

    private var _isProgress = MutableLiveData<Boolean>()
    val isProgress: LiveData<Boolean>
        get() = _isProgress

    init
    {
        validates[InfoEnum.NICKNAME] = false
    }

    fun onClearClicked(){
        _navigator.postValue(NavigatorConst.TO_BACK_SCREEN)
    }

    fun onCompleteSignUpClicked(v: View)
    {
        SopoLog.d("onCompleteSignUpClicked()")

        v.requestFocusFromTouch()
        Handler().postDelayed(Runnable { updateNickname(nickname = nickname.value?:"") }, 100)
    }

    private fun updateNickname(nickname: String)
    {
        validates.forEach { (k, v) ->
            if(!v)
            {
                SopoLog.d("${k.NAME} validate is fail")
                return _validateError.postValue(Pair(k, v))
            }
        }

        _isProgress.postValue(true)

        // result가 전부 통과일 때
        CoroutineScope(Dispatchers.IO).launch {
            val res = userRemoteRepo.updateNickname(nickname = nickname)

            if(!res.result)
            {
                _isProgress.postValue(false)
                return@launch _result.postValue(res)
            }

            userLocalRepo.setNickname(nickname = nickname)

            _navigator.postValue(NavigatorConst.TO_COMPLETE)
        }

    }

}