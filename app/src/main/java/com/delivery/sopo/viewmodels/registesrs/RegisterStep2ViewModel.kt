package com.delivery.sopo.viewmodels.registesrs

import android.view.View
import android.widget.ImageView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.bumptech.glide.Glide
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.database.room.RoomActivate
import com.delivery.sopo.enums.FragmentType
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.util.adapters.GridRvAdapter
import com.delivery.sopo.util.fun_util.SingleLiveEvent
import com.delivery.sopo.util.ui_util.FragmentManager
import com.delivery.sopo.util.ui_util.GridSpacingItemDecoration

class RegisterStep2ViewModel : ViewModel()
{
    val TAG = "LOG.SOPO.RegisterVm"

    var courierList = arrayListOf<CourierItem>()
    val itemList = arrayListOf<SelectItem<CourierItem>>()
    var selectedItem = MutableLiveData<SelectItem<CourierItem>?>()
    var waybilNum = MutableLiveData<String>()
    var moveFragment = MutableLiveData<String>()
    var hideKeyboard = SingleLiveEvent<Boolean>()

    val errorMsg = MutableLiveData<String>()

    var adapter =  MutableLiveData<GridRvAdapter>()
    val decoration = GridSpacingItemDecoration(3, 48, true)
    val rowCnt = 3

    init
    {
        moveFragment.value = ""
        hideKeyboard.value = false
    }

    fun initAdapter(_waybilNum: String)
    {
        if(itemList.size <= 0)
        {
            waybilNum.value = _waybilNum

            RoomActivate.recommendAutoCourier(SOPOApp.INSTANCE, waybilNum.value!!, RoomActivate.rowCnt) {

                courierList = it as ArrayList<CourierItem>

                for(item in courierList)
                {
                    itemList.add(SelectItem(item, false))
                }

                adapter.postValue(GridRvAdapter(items = itemList))
            }
        }
    }

    fun onClearClicked()
    {
        moveFragment.value = FragmentManager.currentFragment1st.NAME
    }
}