package com.delivery.sopo.presentation.register.viewmodel

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.data.repositories.local.repository.CarrierDataSource
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.models.Carrier
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.mapper.CarrierMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.presentation.bindings.FocusChangeCallback
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.presentation.models.enums.RegisterNavigation
import com.delivery.sopo.util.SopoLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InputParcelViewModel @Inject constructor(private val carrierDataSource: CarrierDataSource) :
    BaseViewModel() {
    val waybillNum = MutableLiveData<String>()
    val carrier = MutableLiveData<Carrier?>()

    lateinit var parcel: Parcel.Register

    // 가져온 클립보드 문자열
    val clipboardText = MutableLiveData<String>()

    private val _navigator = MutableLiveData<RegisterNavigation>()
    val navigator: LiveData<RegisterNavigation> = _navigator

    val validity = mutableMapOf<InfoEnum, Boolean>()

    private val _invalidity = MutableLiveData<Pair<InfoEnum, Boolean>>()
    val invalidity: LiveData<Pair<InfoEnum, Boolean>> = _invalidity

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: MutableLiveData<Triple<View, Boolean, InfoEnum>> = _focus

    val focusChangeCallback: FocusChangeCallback = FocusChangeCallback@{ v, hasFocus, type ->
        _focus.value = (Triple(v, hasFocus, type))
    }

    init {
        validity[InfoEnum.WAYBILL_NUMBER] = false
    }

    fun onPasteWaybillClicked() {
        waybillNum.value = clipboardText.value
    }

    fun onSelectCarrierClicked() =checkEventStatus(true) {
        validity.forEach { (k, v) -> if (!v) return@checkEventStatus _invalidity.postValue(Pair(k, v)) }

        parcel = if (::parcel.isInitialized) {
            parcel.copy(waybillNum = waybillNum.value!!, carrier = carrier.value?.carrier)
        } else {
            Parcel.Register(waybillNum = waybillNum.value!!, carrier = carrier.value?.carrier)
        }

        _navigator.postValue(RegisterNavigation.Next("SELECT_CARRIER", parcel))
    }

    fun onConfirmParcelClicked() =checkEventStatus(true){
        validity.forEach { (k, v) -> if (!v) return@checkEventStatus _invalidity.postValue(Pair(k, v)) }

        parcel = if (!::parcel.isInitialized) {
            parcel.copy(waybillNum = waybillNum.value!!, carrier = carrier.value?.carrier)
        } else {
            Parcel.Register(waybillNum = waybillNum.value!!, carrier = carrier.value?.carrier)
        }

        _navigator.postValue(RegisterNavigation.Next("CONFIRM_PARCEL", parcel))
    }

    fun recommendCarrier(waybillNum: String) = scope.launch(Dispatchers.Default) {
        val carrier = carrierDataSource.recommendCarrier(waybillNum)

        SopoLog.d("추천 택배사 :: ${carrier?.toString()}")

        if (carrier == null) {
            this@InputParcelViewModel.carrier.postValue(null)
            return@launch
        }

        this@InputParcelViewModel.carrier.postValue(CarrierMapper.enumToObject(carrier))
    }

    override fun onCleared() {
        super.onCleared()
    }
}

