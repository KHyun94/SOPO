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
import com.delivery.sopo.extensions.md5
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.networks.dto.joins.JoinInfoDTO
import com.delivery.sopo.networks.repository.JoinRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SignUpViewModel(private val userLocalRepo: UserLocalRepository) : ViewModel()
{
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val rePassword = MutableLiveData<String>()

    val validities = mutableMapOf<InfoEnum, Boolean>()

    private var _invalid = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val invalid: LiveData<Pair<InfoEnum, Boolean>>
        get() = _invalid

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
        validities[InfoEnum.EMAIL] = false
        validities[InfoEnum.PASSWORD] = false
        validities[InfoEnum.RE_PASSWORD] = false
        validities[InfoEnum.AGREEMENT] = false
    }


    // 개인정보 동의 이벤트
    fun onAgreeClicked(v: View)
    {
        val cb = v as AppCompatCheckBox
        validities[InfoEnum.AGREEMENT] = cb.isChecked
    }

    fun onSignUpClicked(v: View)
    {
        SopoLog.d("onSignUpClicked() call")
        _isProgress.postValue(true)

        validities.forEach { (k, v) ->
            if(!v)
            {
                SopoLog.d("${k.NAME} validate is fail")
                _isProgress.postValue(false)
                _invalid.postValue(Pair(k, v))
                return@onSignUpClicked
            }
        }

        val joinInfoDTO = JoinInfoDTO(email.value.toString().trim(), password.value.toString().trim().md5(), SOPOApp.deviceInfo)

        CoroutineScope(Dispatchers.Main).launch {
            val res = JoinRepository.requestJoinBySelf(joinInfoDTO)

            _isProgress.postValue(false)

            SopoLog.d("""
                SignUp(Self) Result >>> 
                ${res.result}
                ${res.data}
                ${res.message}
                ${res.displayType}
                ${email.value.toString()}
                ${password.value.toString()}
            """.trimIndent())

            if(!res.result)
            {

                return@launch
            }


            userLocalRepo.setUserId(userId = email.value.toString())
            userLocalRepo.setUserPassword(password = password.value.toString())

            SopoLog.d("""
                    User Data
                    email >>> ${email.value.toString()}, save(${userLocalRepo.getUserId()})
                    password >>> ${password.value.toString()}, save(${userLocalRepo.getUserPassword()})
                """.trimIndent())

            _result.postValue(res)
        }
    }
}