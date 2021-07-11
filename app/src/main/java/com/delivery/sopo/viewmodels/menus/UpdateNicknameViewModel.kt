package com.delivery.sopo.viewmodels.menus

import android.os.Handler
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateNicknameViewModel(private val userLocalRepository: UserLocalRepository): ViewModel()
{
    val nickname = MutableLiveData<String>()
    val validates = mutableMapOf<InfoEnum, Boolean>()

    private var _validateError = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val validateError: LiveData<Pair<InfoEnum, Boolean>>
        get() = _validateError

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

    fun onCompleteSignUpClicked(v: View)
    {
        SopoLog.d("onCompleteSignUpClicked()")
        validates.forEach { (k, v) ->
            if (!v)
            {
                SopoLog.d("${k.NAME} validate is fail")
                _validateError.postValue(Pair(k, v))
                return@onCompleteSignUpClicked
            }
        }

        // result가 전부 통과일 때
        _isProgress.postValue(true)

        CoroutineScope(Dispatchers.IO).launch {
            _result.postValue(updateNickname(nickname = nickname.value.toString()))
        }
    }

    private suspend fun updateNickname(nickname: String): ResponseResult<String>
    {
        return when (val result = UserCall.updateNickname(nickname))
        {
            is NetworkResult.Success ->
            {
                _isProgress.postValue(false)
                userLocalRepository.setNickname(nickname)
                SopoLog.d("Success to update nickname")
                ResponseResult(true, null, nickname, "Success to update nickname")
            }
            is NetworkResult.Error ->
            {
                _isProgress.postValue(false)
                SopoLog.e("Fail to update nickname")
                ResponseResult(false, null, "", "Fail to update nickname", DisplayEnum.DIALOG)
            }
        }
    }

}