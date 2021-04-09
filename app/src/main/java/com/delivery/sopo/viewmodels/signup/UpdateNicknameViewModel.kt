package com.delivery.sopo.viewmodels.signup

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.networks.handler.ResponseHandler
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.SopoLog
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

    val callback: FocusChangeCallback = FocusChangeCallback@{ type, focus ->
        if (focus)
        {
            SopoLog.d("Focus In")

            setVisibleState(type = type, errorState = View.GONE, corState = View.GONE)
            statusType.value = CustomEditText.STATUS_COLOR_BLUE
            return@FocusChangeCallback
        }

        SopoLog.d("Focus Out")

        if(nickname.value == null || nickname.value?.length == 0)
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

    fun onCompleteSignUpClicked(v:View)
    {
        v.requestFocusFromTouch()
        updateNickname(nickname = nickname.value.toString())
    }

    private fun updateNickname(nickname: String)
    {
        CoroutineScope(Dispatchers.IO).launch {
            when(val result = UserCall.updateNickname(nickname))
            {
                is NetworkResult.Success ->
                {
                    userRepoImpl.setNickname(nickname)
                    SopoLog.d("Success to update nickname")

                }
                is NetworkResult.Error ->
                {
                    SopoLog.e("Fail to update nickname")
                }
            }
        }

    }

}