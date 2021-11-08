package com.delivery.sopo.viewmodels.registesrs

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.ParcelExceptionHandler
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.data.repository.local.repository.CarrierRepository
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.InfoEnum
import com.delivery.sopo.enums.NavigatorEnum
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.CarrierDTO
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*


class InputParcelViewModel(private val carrierRepository: CarrierRepository): BaseViewModel()
{
    val waybillNum = MutableLiveData<String>().apply { value = "" }
    val carrier = MutableLiveData<CarrierDTO?>()

    // 가져온 클립보드 문자열
    val clipboardText = MutableLiveData<String>().apply { value = "" }

    private val _navigator = MutableLiveData<NavigatorEnum?>()
    val navigator: LiveData<NavigatorEnum?>
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

    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        ParcelExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }

    private val onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onFailure(error: ErrorEnum) {  }
    }

    init
    {
        validity[InfoEnum.WAYBILL_NUMBER] = false
    }

    fun setNavigator(nav: NavigatorEnum?){
        _navigator.postValue(nav)
    }

    fun onMoveCarrierSelectorClicked()
    {
        _navigator.postValue(NavigatorEnum.REGISTER_SELECT)
    }

    fun onMove3rdStepClicked(v: View) = checkStatus(checkNetwork = true) {
        SopoLog.i("onMove3rdStepClicked(...) 호출")
        validity.forEach { (k, v) ->
            if(!v)
            {
                return@checkStatus _invalidity.postValue(Pair(k, v))
            }
        }

        _navigator.postValue(NavigatorEnum.REGISTER_CONFIRM)
    }


    fun recommendCarrierByWaybill(waybillNum: String) = scope.launch(Dispatchers.Default) {
        val carrier = carrierRepository.recommendAutoCarrier(waybillNum, 1).apply {
            if(size == 0) return@launch
        }.first()

        this@InputParcelViewModel.carrier.postValue(carrier)
    }

    fun onPasteClicked()
    {
        waybillNum.value = clipboardText.value
    }

    override fun onCleared()
    {
        super.onCleared()
        _navigator.value = null
    }
}

