package com.delivery.sopo.viewmodels.signup

import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.JoinRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.exceptions.APIBetaException
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.networks.dto.joins.JoinInfoDTO
import com.delivery.sopo.networks.repository.JoinRepositoryImpl
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow


class SignUpViewModel(private val userLocalRepo: UserLocalRepository, joinRepoImpl: JoinRepositoryImpl):
        ViewModel()
{
    private val joinRepo: JoinRepository = joinRepoImpl

    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val rePassword = MutableLiveData<String>()

    val validity = mutableMapOf<InfoEnum, Boolean>()

    private var _invalidity = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val invalidity: LiveData<Pair<InfoEnum, Boolean>>
        get() = _invalidity

    private var _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: MutableLiveData<Triple<View, Boolean, InfoEnum>>
        get() = _focus

    var focusChangeCallback: FocusChangeCallback = FocusChangeCallback@{ v, hasFocus, type ->
        _focus.value = (Triple(v, hasFocus, type))
    }

    private val scope: CoroutineScope = viewModelScope


    private var _error = MutableLiveData<Pair<Int, String>>()
    val error: LiveData<Pair<Int, String>>
        get() = _error

    private var _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    init
    {
        validity[InfoEnum.EMAIL] = false
        validity[InfoEnum.PASSWORD] = false
        validity[InfoEnum.RE_PASSWORD] = false
        validity[InfoEnum.AGREEMENT] = false
    }

    val handler = CoroutineExceptionHandler { context, exception ->
        when(exception)
        {
            is APIBetaException ->
            {
                SopoLog.e("API Exception -> ${exception.getStatusCode()} / ${
                    exception.getErrorResponse().toString()
                }")

                if(exception.getStatusCode() == 409 && exception.getErrorResponse().code == 605)
                {
                    SopoLog.e("이미 등록된 유저가 존재합니다.", exception)
                    _error.postValue(Pair(exception.getStatusCode(), "이미 등록된 유저가 존재합니다."))
                }
                else
                {
                    SopoLog.e("왜지 시발", exception)
                    _error.postValue(Pair(exception.getStatusCode(), exception.message
                        ?: "알 수 없는 에러"))
                }
            }
            is InternalServerException ->
            {
                _error.postValue(Pair(500, exception.message))
            }
        }
    }

    // 개인정보 동의 이벤트
    fun onAgreeClicked(v: View)
    {
        val checkBox = v as AppCompatCheckBox
        validity[InfoEnum.AGREEMENT] = checkBox.isChecked
    }

    fun onSignUpClicked(v: View)
    {
        v.requestFocusFromTouch()

        SopoLog.d("onSignUpClicked() 호출")

        _isLoading.postValue(true)

        // 입력값의 유효 처리 여부 확인
        validity.forEach { (k, v) ->
            if(!v) return _invalidity.postValue(Pair(k, v))
        }

        val email = email.value.toString().trim()
        val password = password.value.toString().trim()

        val joinInfoDTO = JoinInfoDTO(email = email, password = password)

        requestSignUp(joinInfoDTO = joinInfoDTO)
    }

    private fun requestSignUp(joinInfoDTO: JoinInfoDTO) = scope.launch(Dispatchers.IO) {

        try
        {
            joinRepo.requestJoinBySelf(joinInfoDTO)
        }
        catch(e: Exception)
        {
            return@launch handler.handleException(coroutineContext, e)
        }

        withContext(Dispatchers.Default){
            userLocalRepo.setUserId(joinInfoDTO.email)
            userLocalRepo.setUserPassword(joinInfoDTO.password)
        }

        _navigator.postValue(NavigatorConst.TO_COMPLETE)
    }
}