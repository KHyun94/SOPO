package com.delivery.sopo.viewmodels.registesrs

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.enums.FragmentType
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.util.adapters.GridRvAdapter
import com.delivery.sopo.util.adapters.ViewPagerAdapter
import com.delivery.sopo.util.fun_util.SingleLiveEvent
import com.delivery.sopo.util.ui_util.FragmentManager
import com.delivery.sopo.util.ui_util.GridSpacingItemDecoration

class RegisterStep2ViewModel : ViewModel()
{
    val TAG = "LOG.SOPO.RegisterVm"

    private val courierList = arrayListOf<CourierItem>()

    var trackNumStr = MutableLiveData<String>()

    var courier = MutableLiveData<CourierItem>()

    var hideKeyboard = SingleLiveEvent<Boolean>()

    var moveFragment = MutableLiveData<String>()

    val errorMsg = MutableLiveData<String>()

    val adapter: GridRvAdapter = GridRvAdapter(courierList)
    val decoration = GridSpacingItemDecoration(3, 10, true)
    val rowCnt = 3


    init
    {
        moveFragment.value = ""
        hideKeyboard.value = false
    }

    fun onMoveStep2Clicked()
    {
        if (trackNumStr.value!!.length > 10)
        {
            if (courier.value == null || courier.value!!.courierName.isEmpty())
                moveFragment.value = FragmentType.REGISTER_STEP2.NAME
        }
        else
        {
            errorMsg.value = "운송장 번호를 입력해주세요."
        }
    }

    fun onClearClicked()
    {
        moveFragment.value = FragmentManager.currentFragment1st.NAME
    }
}