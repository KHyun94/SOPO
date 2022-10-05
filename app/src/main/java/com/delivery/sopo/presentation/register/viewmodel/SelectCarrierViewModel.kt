package com.delivery.sopo.presentation.register.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.data.repositories.local.repository.CarrierDataSource
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.base.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SelectCarrierViewModel @Inject constructor(private val carrierRepo: CarrierDataSource): BaseViewModel()
{
    val waybillNum = MutableLiveData<String>()
    val carrier = MutableLiveData<CarrierEnum>()

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    init
    {
        waybillNum.value = ""
    }

    fun postNavigator(navigator: String){
        _navigator.postValue(navigator)
    }

    suspend fun getCarriers(waybillNum: String): List<SelectItem<Carrier?>>
    {
        return carrierRepo.getAll().map { carrier -> SelectItem(carrier, false) }
    }

    fun onClearClicked()
    {
        _navigator.value = NavigatorConst.REGISTER_INPUT_INFO
    }

    override fun onCleared()
    {
        super.onCleared()
    }

}