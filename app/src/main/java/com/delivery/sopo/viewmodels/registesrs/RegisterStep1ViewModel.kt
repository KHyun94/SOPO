package com.delivery.sopo.viewmodels.registesrs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.enums.FragmentType
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.util.livedates.SingleLiveEvent
import com.delivery.sopo.viewmodels.signup.FocusChangeCallback


class RegisterStep1ViewModel : ViewModel()
{
    val TAG = "LOG.SOPO.RegisterVm"

    private val courierList = mutableListOf<CourierItem>()

    var waybilNum = MutableLiveData<String?>()

    // 가져온 클립보드 문자열
    var clipboardStr = SingleLiveEvent<String>()

    var courier = MutableLiveData<CourierItem?>()

    var waybilNoStatusType = MutableLiveData<Int>()
    var hideKeyboard =
        SingleLiveEvent<Boolean>()

    var moveFragment = MutableLiveData<String>()

    val errorMsg = MutableLiveData<String>()

    init
    {
        moveFragment.value = ""
        waybilNum.value = ""
        clipboardStr.value = ""
        hideKeyboard.value = false
        waybilNoStatusType.value = -1
    }

    var callback: FocusChangeCallback = FocusChangeCallback@{ type, focus ->
        hideKeyboard.value = !focus
    }

    fun getCourierType(courier: String?): CourierItem?
    {
        for (c in courierList) if (courier == c.courierName) return c
        return null
    }

    fun onMoveStep2Clicked()
    {
        if (waybilNum.value!!.length > 8)
        {
            if (courier.value == null || courier.value!!.courierName.isEmpty())
                moveFragment.value = FragmentType.REGISTER_STEP2.NAME
        }
        else
        {
            errorMsg.value = "운송장 번호를 입력해주세요."
        }
    }

    fun onMoveStep3Clicked()
    {
        moveFragment.value = FragmentType.REGISTER_STEP3.NAME
    }

    fun onReselectClicked()
    {
        if (waybilNum.value!!.length > 8)
        {
            if (courier.value != null && !courier.value!!.courierName.isEmpty())
                moveFragment.value = FragmentType.REGISTER_STEP2.NAME
        }
        else
        {
            errorMsg.value = "운송장 번호를 입력해주세요."
        }
    }

    fun onPasteClicked()
    {
        waybilNum.value = clipboardStr.value
        clipboardStr.call()
    }

}