package com.delivery.sopo.viewmodels.registesrs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.data.repository.database.room.RoomActivate
import com.delivery.sopo.models.CarrierDTO
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.data.repository.local.repository.CarrierRepository
import com.delivery.sopo.views.adapter.GridTypedRecyclerViewAdapter
import com.delivery.sopo.util.livedates.SingleLiveEvent
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.setting.GridSpacingItemDecoration
import kotlinx.coroutines.*

class RegisterStep2ViewModel(private val carrierRepo: CarrierRepository): ViewModel()
{
    var itemList = listOf<SelectItem<CarrierDTO?>>()
    var selectedItem = MutableLiveData<SelectItem<CarrierDTO?>>()
    var waybillNum = MutableLiveData<String>()

    var moveFragment = MutableLiveData<String>()

    var adapter = MutableLiveData<GridTypedRecyclerViewAdapter>()
    val decoration = GridSpacingItemDecoration(3, 32, true)
    val rowCnt = 3

    init
    {
        moveFragment.value = ""
        waybillNum.value = ""
    }

    fun setAdapter(_waybillNum: String)
    {
        SopoLog.d("setAdapter >>> $_waybillNum")

        if (itemList.isNotEmpty()) return

        waybillNum.value = _waybillNum

        itemList = if(_waybillNum != "")
        {
            SopoLog.d("운송장 번호 >>> $_waybillNum")
            runBlocking(Dispatchers.Default) {
                RoomActivate.recommendAutoCarrier(waybillNum.value!!, RoomActivate.rowCnt) ?: emptyList<CarrierDTO?>().toMutableList()
            }
        }
        else
        {
            SopoLog.d("운송장 번호 >>> empty")
            runBlocking(Dispatchers.Default) { carrierRepo.getAll().toMutableList() }
        }.flatMap { listOf(SelectItem(it, false)) }

        adapter.postValue(GridTypedRecyclerViewAdapter(items = itemList))
    }

    fun onClearClicked()
    {
        moveFragment.value = FragmentManager.currentFragment1st.NAME
    }
}