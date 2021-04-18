package com.delivery.sopo.viewmodels.registesrs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.database.room.RoomActivate
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.repository.impl.CourierRepoImpl
import com.delivery.sopo.views.adapter.GridRvAdapter
import com.delivery.sopo.util.livedates.SingleLiveEvent
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.setting.GridSpacingItemDecoration

class RegisterStep2ViewModel(private val courierRepoImpl: CourierRepoImpl) : ViewModel()
{
    var courierList = arrayListOf<CourierItem?>()
    var itemList = listOf<SelectItem<CourierItem?>>()
    var selectedItem = MutableLiveData<SelectItem<CourierItem?>>()
    var wayBilNum = MutableLiveData<String>()
    var moveFragment = MutableLiveData<String>()
    var hideKeyboard = SingleLiveEvent<Boolean>()

    val errorMsg = MutableLiveData<String>()

    var adapter =  MutableLiveData<GridRvAdapter>()
    val decoration = GridSpacingItemDecoration(3, 32, true)
//        GridSpacingItemDecoration(3, 48, true)
    val rowCnt = 3

    init
    {
        moveFragment.value = ""
        wayBilNum.value = ""
        hideKeyboard.value = false
    }

    fun setAdapter(_wayBilNum: String)
    {
        if(itemList.isEmpty())
        {
            wayBilNum.value = _wayBilNum

            courierList =  RoomActivate.recommendAutoCourier(SOPOApp.INSTANCE, wayBilNum.value!!, RoomActivate.rowCnt,courierRepoImpl) as ArrayList<CourierItem?>

            itemList = courierList.flatMap {
                listOf(SelectItem(it, false))
            }

            adapter.postValue(GridRvAdapter(items = itemList))
        }
    }

    fun onClearClicked()
    {
        moveFragment.value = FragmentManager.currentFragment1st.NAME
    }
}