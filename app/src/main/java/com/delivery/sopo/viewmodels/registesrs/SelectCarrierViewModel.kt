package com.delivery.sopo.viewmodels.registesrs

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.exceptions.UserExceptionHandler
import com.delivery.sopo.data.repository.local.repository.CarrierRepository
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.NavigatorEnum
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.base.BaseViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers

class SelectCarrierViewModel(private val carrierRepo: CarrierRepository): BaseViewModel()
{
    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        UserExceptionHandler(Dispatchers.Main, object: OnSOPOErrorCallback
        {
            override fun onFailure(error: ErrorEnum)
            {
            }
        })
    }

//    var selectedItem = MutableLiveData<SelectItem<CarrierDTO?>>()
    val waybillNum = MutableLiveData<String>()
    val carrier = MutableLiveData<CarrierEnum>()

    private val _navigator = MutableLiveData<NavigatorEnum?>()
    val navigator: LiveData<NavigatorEnum?>
        get() = _navigator

    init
    {
        waybillNum.value = ""
    }

    fun setNavigator(nav: NavigatorEnum?){
        _navigator.postValue(nav)
    }

    suspend fun getCarriers(waybillNum: String): List<SelectItem<Carrier?>>
    {
        val list = if(waybillNum.isEmpty()) carrierRepo.recommendAutoCarrier(waybillNum, 27)
        else carrierRepo.getAll().toMutableList()

        return list.map {
            SelectItem(it, false)
        }
    }

    fun onClearClicked()
    {
        _navigator.value = NavigatorEnum.REGISTER_INPUT
    }

    override fun onCleared()
    {
        super.onCleared()
        _navigator.value = null
    }

}