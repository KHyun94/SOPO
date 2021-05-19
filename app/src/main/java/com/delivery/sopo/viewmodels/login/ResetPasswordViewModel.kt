package com.delivery.sopo.viewmodels.login

import android.os.Handler
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.extensions.toMD5
import com.delivery.sopo.models.PasswordResetDTO
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.views.widget.CustomEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResetPasswordViewModel: ViewModel()
{
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    val resetType = MutableLiveData<Int>()
    // 0: 이메일 전송 1: 패스워드 입력 2: 완료

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

    var jwtTokenForReset: String? = null

    init
    {
        resetType.value = 0

    }

    fun onSendEmailClicked(v: View)
    {
        validates.forEach { (k, v) ->
            if (!v)
            {
                SopoLog.d("${k.NAME} validate is fail")
                _validateError.postValue(Pair(k, v))
                return@onSendEmailClicked
            }
        }

        CoroutineScope(Dispatchers.IO).launch {

            val resetType = resetType.value

            when(resetType)
            {
                0->
                {
                    val res = UserRemoteRepository.requestEmailForAuth(email = email.value?:"")
                    SopoLog.d("res >>> ${res.toString()} ${res.code} ${res.data} ${res.result} ${res.message}")

                    if(res.result) jwtTokenForReset = res.data?.token
                    _result.postValue(res)
                }
                1->
                {
                    val passwordResetDTO = PasswordResetDTO(jwtTokenForReset?:"", email.value.toString(), password.value.toString().toMD5())

                    val res = UserRemoteRepository.requestPasswordForReset(passwordResetDTO = passwordResetDTO)

                    _result.postValue(res)
                }
            }
        }
    }


}