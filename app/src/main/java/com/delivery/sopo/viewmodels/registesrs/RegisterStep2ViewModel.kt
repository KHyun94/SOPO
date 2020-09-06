package com.delivery.sopo.viewmodels.registesrs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.database.room.RoomActivate
import com.delivery.sopo.enums.FragmentType
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.util.adapters.GridRvAdapter
import com.delivery.sopo.util.fun_util.SingleLiveEvent
import com.delivery.sopo.util.ui_util.FragmentManager
import com.delivery.sopo.util.ui_util.GridSpacingItemDecoration

class RegisterStep2ViewModel : ViewModel()
{
    val TAG = "LOG.SOPO.RegisterVm"

    private var courierList = arrayListOf<CourierItem>()

    var trackNumStr = MutableLiveData<String>()

    var courier = MutableLiveData<CourierItem>()

    var hideKeyboard = SingleLiveEvent<Boolean>()

    var moveFragment = MutableLiveData<String>()

    val errorMsg = MutableLiveData<String>()

    var adapter =  MutableLiveData<GridRvAdapter>()
    val decoration = GridSpacingItemDecoration(3, 10, true)
    val rowCnt = 3

    init
    {
        moveFragment.value = ""
        hideKeyboard.value = false
    }

    fun initAdapter(waybilNum: String)
    {
        trackNumStr.value = waybilNum

        RoomActivate.recommendAutoCourier(SOPOApp.INSTANCE, waybilNum, RoomActivate.rowCnt) {
            courierList = it as ArrayList<CourierItem>
            adapter.postValue(GridRvAdapter(items = courierList))
        }
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