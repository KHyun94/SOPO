package com.delivery.sopo.presentation.viewmodels.registesrs

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repositories.local.repository.CarrierRepository
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.mapper.CarrierMapper
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*

class InputParcelViewModel(private val carrierRepository: CarrierRepository): BaseViewModel()
{
    val waybillNum = MutableLiveData<String>()
    val carrier = MutableLiveData<Carrier?>()

    // 가져온 클립보드 문자열
    val clipboardText = MutableLiveData<String>()

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    val validity = mutableMapOf<InfoEnum, Boolean>()

    private val _invalidity = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val invalidity: LiveData<Pair<InfoEnum, Boolean>>
        get() = _invalidity

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: MutableLiveData<Triple<View, Boolean, InfoEnum>>
        get() = _focus

    val focusChangeCallback: FocusChangeCallback = FocusChangeCallback@{ v, hasFocus, type ->
        _focus.value = (Triple(v, hasFocus, type))
    }

    init
    {
        validity[InfoEnum.WAYBILL_NUMBER] = false
    }

    fun postNavigator(navigator: String){
        _navigator.postValue(navigator)
    }

    fun onMoveCarrierSelectorClicked()
    {
        postNavigator(NavigatorConst.REGISTER_SELECT_CARRIER)
    }

    fun onMove3rdStepClicked(v: View) = checkEventStatus(checkNetwork = true) {
        SopoLog.i("onMove3rdStepClicked(...) 호출")
        validity.forEach { (k, v) ->
            if(!v)
            {
                return@checkEventStatus _invalidity.postValue(Pair(k, v))
            }
        }

        postNavigator(NavigatorConst.REGISTER_CONFIRM_PARCEL)
    }

    fun recommendCarrierByWaybill(waybillNum: String) = scope.launch(Dispatchers.Default) {
        val carrier = carrierRepository.recommendCarrier(waybillNum)

        SopoLog.d("추천 택배사 :: ${carrier?.toString()}")

        if(carrier == null)
        {
            this@InputParcelViewModel.carrier.postValue(null)
            return@launch
        }

        this@InputParcelViewModel.carrier.postValue(CarrierMapper.enumToObject(carrier))
    }

    fun onPasteClicked()
    {
        waybillNum.value = clipboardText.value
    }

    override fun onCleared()
    {
        super.onCleared()
    }
}

