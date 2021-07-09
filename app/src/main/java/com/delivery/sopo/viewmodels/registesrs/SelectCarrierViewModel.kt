package com.delivery.sopo.viewmodels.registesrs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.data.repository.database.room.RoomActivate
import com.delivery.sopo.models.CarrierDTO
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.data.repository.local.repository.CarrierRepository
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.views.adapter.GridTypedRecyclerViewAdapter
import com.delivery.sopo.util.livedates.SingleLiveEvent
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.setting.GridSpacingItemDecoration
import kotlinx.coroutines.*

class SelectCarrierViewModel(private val carrierRepo: CarrierRepository): ViewModel()
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

    fun setCarrierAdapter(_waybillNum: String)
    {
        SopoLog.d("setCarrierAdapter >>> $_waybillNum")

        if (itemList.isNotEmpty()) return

        waybillNum.value = _waybillNum

        itemList = runBlocking(Dispatchers.Default) {
            return@runBlocking if(waybillNum.value != null && waybillNum.value != "")
            {
                RoomActivate.recommendAutoCarrier(waybillNum.value!!, RoomActivate.rowCnt) ?: emptyList<CarrierDTO?>().toMutableList()
            }
            else
            {
                carrierRepo.getAll().toMutableList()
            }
            }.flatMap { listOf(SelectItem(it, false))
        }

        adapter.postValue(GridTypedRecyclerViewAdapter(items = itemList))
    }

    fun onClearClicked()
    {
        moveFragment.value = TabCode.REGISTER_INPUT.NAME
    }
}