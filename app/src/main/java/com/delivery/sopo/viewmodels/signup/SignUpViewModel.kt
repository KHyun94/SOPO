package com.delivery.sopo.viewmodels.signup

import android.os.Handler
import android.view.View
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.networks.dto.joins.JoinInfoByKakaoDTO
import com.delivery.sopo.networks.repository.JoinRepository
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUpViewModel(private val userRepo: UserRepoImpl) : ViewModel()
{
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val rePassword = MutableLiveData<String>()

    val validates = mutableMapOf<InfoEnum, Boolean>()

    private var _validateError = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val validateError: LiveData<Pair<InfoEnum, Boolean>>
        get() = _validateError

    /**
     * 유효성 및 통신 등의 결과 객체
     */
    private var _result = MutableLiveData<ResponseResult<*>>()
    val result: LiveData<ResponseResult<*>>
        get() = _result

    private var _isProgress = MutableLiveData<Boolean>()
    val isProgress: LiveData<Boolean>
        get() = _isProgress

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: MutableLiveData<Triple<View, Boolean, InfoEnum>>
        get() = _focus

    var focusChangeCallback: FocusChangeCallback = FocusChangeCallback@{ v, hasFocus, type->
        SopoLog.i("${type.NAME} >>> $hasFocus")
        Handler().postDelayed(Runnable {
            _focus.value = (Triple(v, hasFocus, type))
        }, 50)
    }

    init
    {
        validates[InfoEnum.EMAIL] = false
        validates[InfoEnum.PASSWORD] = false
        validates[InfoEnum.RE_PASSWORD] = false
        validates[InfoEnum.AGREEMENT] = false
    }


    // 개인정보 동의 이벤트
    fun onAgreeClicked(v: View)
    {
        val cb = v as AppCompatCheckBox
        SopoLog.d("약관동의 >>> ${cb.isChecked}")
        validates[InfoEnum.AGREEMENT] = true
    }

    fun onSignUpClicked(v: View)
    {
        _isProgress.postValue(true)

        validates.forEach { (k, v) ->
            if(!v)
            {
                SopoLog.d("${k.NAME} validate is fail")
                _isProgress.postValue(false)
                _validateError.postValue(Pair(k, v))
                return@onSignUpClicked
            }
        }

        val joinInfoByKakaoDTO = JoinInfoByKakaoDTO(email.value.toString(), password.value.toString(), SOPOApp.deviceInfo)

        CoroutineScope(Dispatchers.Main).launch {
            val res = JoinRepository.requestJoinBySelf(joinInfoByKakaoDTO)

            _isProgress.postValue(false)

            SopoLog.d("""
                회원가입 결과 >>> 
                ${res.result}
                ${res.data}
                ${res.message}
                ${res.displayType}
                ${email.value.toString()}
                ${password.value.toString()}
            """.trimIndent())

            if(res.result)
            {
                userRepo.setEmail(email = email.value.toString())
                userRepo.setApiPwd(pwd = password.value.toString())

                SopoLog.d("""
                    User Data
                    email >>> ${email.value.toString()}, save(${userRepo.getEmail()})
                    password >>> ${password.value.toString()}, save(${userRepo.getApiPwd()})
                """.trimIndent())
            }

            _result.postValue(res)
        }
    }
}