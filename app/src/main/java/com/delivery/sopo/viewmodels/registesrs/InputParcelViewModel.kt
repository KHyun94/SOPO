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
import kotlinx.coroutines.*


class InputParcelViewModel(private val carrierRepository: CarrierRepository): BaseViewModel()
{
    var waybillNum = MutableLiveData<String>().apply { value = "" }
    var carrier = MutableLiveData<CarrierDTO?>()

    // 가져온 클립보드 문자열
    var clipboardText = MutableLiveData<String>().apply { value = "" }

    val errorMsg = MutableLiveData<String>()

    private var _navigator = MutableLiveData<NavigatorEnum>()
    val navigator: LiveData<NavigatorEnum>
        get() = _navigator

    val validity = mutableMapOf<InfoEnum, Boolean>()

    private var _invalidity = MutableLiveData<Pair<InfoEnum, Boolean>>()
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

    fun onMoveCarrierSelectorClicked()
    {
        _navigator.postValue(NavigatorEnum.REGISTER_SELECT)
        //        moveFragment.value = TabCode.REGISTER_SELECT.NAME
    }

    fun onMove3rdStepClicked(v: View)
    {
        v.requestFocusFromTouch()

        validity.forEach { (k, v) ->
            if(!v)
            {
                return _invalidity.postValue(Pair(k, v))
            }
        }

        scope.launch(Dispatchers.Main) {

            v.clearFocus()

            val carrierCode = carrier.value!!.carrier.CODE
            val waybillNum = waybillNum.value

            carrierRepository.getCarrierEntityWithCode(carrierCode)

            _navigator.postValue(NavigatorEnum.REGISTER_CONFIRM)
        }

    }

    fun recommendCarrierByWaybill(waybillNum: String) = scope.launch(Dispatchers.Default) {
        val carrier = carrierRepository.recommendAutoCarrier(waybillNum, 1).apply {
            if(size == 0) return@launch
        }.first()

        this@InputParcelViewModel.carrier.postValue(carrier)
    }

    // clipBoardWord(클립보드에 저장된 값)을 waybillNum(택배 운송장 번호) 입력 란으로 삽입
    fun onPasteClicked()
    {
        waybillNum.value = clipboardText.value
    }

    private val onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onFailure(error: ErrorEnum) { }
    }

    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        ParcelExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }
}

