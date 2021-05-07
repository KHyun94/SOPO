package com.delivery.sopo.viewmodels.login

import android.os.Handler
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.InfoEnum
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

    init
    {
        validates[InfoEnum.EMAIL] = false
    }

    fun onSendEmailClicked(v: View)
    {
        _result.postValue(ResponseResult(true, null, Unit, "정보 입력을 완료해주세요.", DisplayEnum.TOAST_MESSAGE))
        CoroutineScope(Dispatchers.IO).launch {
//            _result.postValue(updateNickname(nickname = nickname.value.toString()))
        }
    }


}