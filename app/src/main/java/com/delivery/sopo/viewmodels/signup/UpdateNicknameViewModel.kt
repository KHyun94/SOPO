package com.delivery.sopo.viewmodels.signup

import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.networks.handler.ResponseHandler
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.views.widget.CustomEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UpdateNicknameViewModel(private val userRepoImpl: UserRepoImpl): ViewModel()
{
    val nickname = MutableLiveData<String>()
    val statusType = MutableLiveData<Int>()
    val isCorrectVisible = MutableLiveData<Int>()
    val isErrorVisible = MutableLiveData<Int>()

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

        if (nickname.value == null || nickname.value?.length == 0)
        {
            SopoLog.d("Fail to check validate")
            setVisibleState(type = InfoConst.NICKNAME, errorState = View.VISIBLE, corState = View.GONE)
            statusType.value = CustomEditText.STATUS_COLOR_RED

            return@FocusChangeCallback
        }

        SopoLog.d("Success to check validate")
        setVisibleState(type = InfoConst.NICKNAME, errorState = View.GONE, corState = View.VISIBLE)
        statusType.value = CustomEditText.STATUS_COLOR_BLUE
    }

    init
    {
        setVisibleState(InfoConst.NICKNAME, View.GONE, View.GONE)
        statusType.value = CustomEditText.STATUS_COLOR_ELSE
    }

    fun setVisibleState(type: String, errorState: Int, corState: Int)
    {
        when (type)
        {
            InfoConst.NICKNAME ->
            {
                isErrorVisible.value = (errorState)
                isCorrectVisible.value = (corState)
            }
        }
    }

    fun onCompleteSignUpClicked(v: View)
    {
        v.requestFocusFromTouch()

        val isValidate = ValidateUtil.isValidateNickname(nickname = nickname.value.toString())

        if(!isValidate)
        {
            _result.postValue(ResponseResult(false, null, Unit, "정보 입력을 완료해주세요.", DisplayEnum.TOAST_MESSAGE))
            return
        }

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
                userRepoImpl.setNickname(nickname)
                SopoLog.d("Success to update nickname")
                ResponseResult(true, null, nickname, "Success to update nickname")
            }
            is NetworkResult.Error ->
            {
                SopoLog.e("Fail to update nickname")
                ResponseResult(false, null, "", "Fail to update nickname", DisplayEnum.DIALOG)
            }
        }
    }

}