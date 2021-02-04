package com.delivery.sopo.viewmodels.registesrs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.isGreaterThanOrEqual
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.util.livedates.SingleLiveEvent
import com.delivery.sopo.viewmodels.signup.FocusChangeCallback
import com.delivery.sopo.views.widget.CustomEditText


class RegisterStep1ViewModel : ViewModel()
{
    val TAG = this.javaClass.simpleName

    var wayBilNum = MutableLiveData<String?>()

    // 가져온 클립보드 문자열
    var clipBoardWords = SingleLiveEvent<String>()

    var courier = MutableLiveData<CourierItem?>()

    var wayBilNumStatusType = MutableLiveData<Int>()

    var moveFragment = MutableLiveData<String>()

    val errorMsg = MutableLiveData<String>()

    var callback : FocusChangeCallback = { type, focus ->

        if (focus)
        {
            wayBilNumStatusType.postValue(CustomEditText.STATUS_COLOR_BLUE)
        }
        else
        {
            wayBilNumStatusType.run{
                if (wayBilNum.value.isGreaterThanOrEqual(1)) postValue((CustomEditText.STATUS_COLOR_BLACK))
                else postValue(CustomEditText.STATUS_COLOR_ELSE)
            }
        }

    }

    init
    {
        moveFragment.value = ""
        wayBilNum.value = ""
        clipBoardWords.value = ""
        wayBilNumStatusType.value = CustomEditText.STATUS_COLOR_ELSE
    }

    fun onMoveFinalStepClicked()
    {
        moveFragment.value = TabCode.REGISTER_STEP3.NAME
    }

    fun onReselectCourierClicked()
    {
        if (wayBilNum.value.isGreaterThanOrEqual(9))
        {
            if (courier.value != null && courier.value!!.courierName.isNotEmpty()) moveFragment.value =
                TabCode.REGISTER_STEP2.NAME
        }
        else
        {
            errorMsg.value = "운송장 번호를 입력해주세요."
        }
    }

    // clipBoardWord(클립보드에 저장된 값)을 wayBilNum(택배 운송장 번호) 입력 란으로 삽입
    fun onPasteClicked()
    {
        wayBilNum.value = clipBoardWords.value
        clipBoardWords.call()
    }

}

