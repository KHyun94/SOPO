package com.delivery.sopo.viewmodels.registesrs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.data.repository.database.room.RoomActivate
import com.delivery.sopo.models.CarrierDTO
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.data.repository.local.repository.CarrierRepository
import com.delivery.sopo.views.adapter.GridRvAdapter
import com.delivery.sopo.util.livedates.SingleLiveEvent
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.setting.GridSpacingItemDecoration

class RegisterStep2ViewModel(private val carrierRepo: CarrierRepository): ViewModel()
{
    var carrierList = mutableListOf<CarrierDTO?>()
    var itemList = listOf<SelectItem<CarrierDTO?>>()
    var selectedItem = MutableLiveData<SelectItem<CarrierDTO?>>()
    var waybillNum = MutableLiveData<String>()
    var moveFragment = MutableLiveData<String>()
    var hideKeyboard = SingleLiveEvent<Boolean>()

    val errorMsg = MutableLiveData<String>()

    var adapter = MutableLiveData<GridRvAdapter>()
    val decoration = GridSpacingItemDecoration(3, 32, true)
    val rowCnt = 3

    init
    {
        moveFragment.value = ""
        waybillNum.value = ""
        hideKeyboard.value = false
    }

    fun setAdapter(_waybillNum: String)
    {
        if (itemList.isEmpty())
        {
            waybillNum.value = _waybillNum

            carrierList =
                RoomActivate.recommendAutoCarrier(SOPOApp.INSTANCE, waybillNum.value!!, RoomActivate.rowCnt, carrierRepo)
                    ?: return

            itemList = carrierList.flatMap {
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