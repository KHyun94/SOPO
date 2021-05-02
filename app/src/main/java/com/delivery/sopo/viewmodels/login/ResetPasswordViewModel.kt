package com.delivery.sopo.viewmodels.login

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.views.registers.FocusChangeCallback
import com.delivery.sopo.views.widget.CustomEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ResetPasswordViewModel: ViewModel()
{
    val email = MutableLiveData<String>()
    val validateText = MutableLiveData<String>()

    val isErrorVisible = MutableLiveData<Int>()
    val isCorVisible = MutableLiveData<Int>()

    val statusType = MutableLiveData<Int>()

    // 유효성 및 통신 등의 결과 객체
    private var _result = MutableLiveData<ResponseResult<*>>()
    val result: LiveData<ResponseResult<*>>
        get() = _result

    val callback: FocusChangeCallback = FocusChangeCallback@{ type, focus ->
        if (focus)
        {
            SopoLog.d("Focus In")

            setVisibleState(type = type, errorState = View.GONE, corState = View.GONE)
            statusType.value = CustomEditText.STATUS_COLOR_BLUE
            return@FocusChangeCallback
        }

        SopoLog.d("Focus Out")

        if (!ValidateUtil.isValidateEmail(email = email.value.toString()))
        {
            SopoLog.d("Fail to check validate")
            setVisibleState(type = InfoConst.EMAIL, errorState = View.VISIBLE, corState = View.GONE)
            statusType.value = CustomEditText.STATUS_COLOR_RED
            validateText.postValue("잘못된 이메일 양식입니다.")

            return@FocusChangeCallback
        }

        SopoLog.d("Success to check validate")
        setVisibleState(type = InfoConst.EMAIL, errorState = View.GONE, corState = View.VISIBLE)
        statusType.value = CustomEditText.STATUS_COLOR_BLUE
    }

    init
    {
        setVisibleState(InfoConst.EMAIL, View.GONE, View.GONE)
        validateText.value = ""
        statusType.value = CustomEditText.STATUS_COLOR_ELSE
    }

    fun setVisibleState(type: String, errorState: Int, corState: Int)
    {
        when (type)
        {
            InfoConst.EMAIL ->
            {
                isErrorVisible.value = (errorState)
                isCorVisible.value = (corState)
            }
        }
    }

    fun onSendEmailClicked(v: View)
    {
        v.requestFocusFromTouch()

        val isValidate = ValidateUtil.isValidateEmail(email = email.value.toString())

        if(!isValidate)
        {
            _result.postValue(ResponseResult(false, null, Unit, "정보 입력을 완료해주세요.", DisplayEnum.TOAST_MESSAGE))
            return
        }

        _result.postValue(ResponseResult(true, null, Unit, "정보 입력을 완료해주세요.", DisplayEnum.TOAST_MESSAGE))
        CoroutineScope(Dispatchers.IO).launch {
//            _result.postValue(updateNickname(nickname = nickname.value.toString()))
        }
    }


}