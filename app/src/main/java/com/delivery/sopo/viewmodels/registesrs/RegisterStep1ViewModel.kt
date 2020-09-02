package com.delivery.sopo.viewmodels.registesrs

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.database.room.RoomActivate
import com.delivery.sopo.enums.FragmentType
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.util.adapters.GridRvAdapter
import com.delivery.sopo.util.fun_util.SingleLiveEvent
import com.delivery.sopo.util.ui_util.FragmentManager
import com.delivery.sopo.util.ui_util.GridSpacingItemDecoration
import com.delivery.sopo.viewmodels.FocusChangeCallback


class RegisterStep1ViewModel : ViewModel()
{
    val TAG = "LOG.SOPO.RegisterVm"

    private val courierList = mutableListOf<CourierItem>()

    var trackNumStr = MutableLiveData<String>()

    // 가져온 클립보드 문자열
    var clipboardStr = SingleLiveEvent<String>()

    var courier = MutableLiveData<CourierItem>()

    var hideKeyboard = SingleLiveEvent<Boolean>()

    var moveFragment = MutableLiveData<String>()

    val errorMsg = MutableLiveData<String>()

    init
    {
        moveFragment.value = ""
        trackNumStr.value = ""
        clipboardStr.value = ""
        hideKeyboard.value = false
    }

    var callback: FocusChangeCallback = FocusChangeCallback@{ type, focus ->
        hideKeyboard.value = !focus
    }

    fun getCourierType(courier: String?): CourierItem?
    {
        for (c in courierList)
        {
            if (courier == c.courierName)
            {
                return c
            }
        }

        return null
    }

    fun onMoveStep2Clicked()
    {
        if (trackNumStr.value!!.length > 8)
        {
            if (courier.value == null || courier.value!!.courierName.isEmpty())
                moveFragment.value = FragmentType.REGISTER_STEP2.NAME
        }
        else
        {
            errorMsg.value = "운송장 번호를 입력해주세요."
        }
    }

    fun onReselectClicked()
    {
        if (trackNumStr.value!!.length > 8)
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
        trackNumStr.value = clipboardStr.value
        clipboardStr.call()
    }

}