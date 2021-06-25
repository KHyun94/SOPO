package com.delivery.sopo.viewmodels.registesrs

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.remote.parcel.ParcelRemoteRepository
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.models.*
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConfirmParcelViewModel: ViewModel()
{
    var waybillNum = MutableLiveData<String>()
    var carrier = MutableLiveData<CarrierDTO>()
    var alias = MutableLiveData<String>()

    private var _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    val isProgress = MutableLiveData<Boolean>()

    private var _result = MutableLiveData<ResponseResult<Int?>>()
    val result: LiveData<ResponseResult<Int?>>
        get() = _result

    init
    {
        isProgress.value = false
    }

    fun onMoveFirstStep(v: View)
    {
        when(v.id)
        {
            R.id.tv_revise ->
            {
                _navigator.value = NavigatorConst.TO_REGISTER_REVISE
            }
            R.id.tv_init ->
            {
                _navigator.value = NavigatorConst.TO_REGISTER_INIT
            }
            R.id.tv_register ->
            {
                isProgress.postValue(true)

                val registerDTO = ParcelRegisterDTO(carrier = carrier.value?.carrier?:throw Exception("Carrier must be not null"),
                                                    waybillNum = waybillNum.value.toString(),
                                                    alias = alias.value.toString())

                CoroutineScope(Dispatchers.IO).launch {
                    requestParcelRegister(registerDTO = registerDTO)
                }
            }
        }
    }

    // '등록하기' Button Click event
    private suspend fun requestParcelRegister(registerDTO: ParcelRegisterDTO)
    {
        SopoLog.d("requestParcelRegister(...) 호출[${registerDTO.toString()}]")

        val res = ParcelRemoteRepository.requestParcelRegister(registerDTO = registerDTO)

        isProgress.postValue(false)

        if(!res.result){
            SopoLog.e("requestParcelRegister(...) 실패[code:${res.code}][message:${res.message}]")
            return _result.postValue(res)
        }

        if(res.data == null)
        {
            SopoLog.e("requestParcelRegister(...) 실패[code:${res.code}][message:${res.message}]")
            return _result.postValue(ResponseResult(false, null, null, "서버 오류.", DisplayEnum.DIALOG))
        }

        _navigator.postValue(NavigatorConst.TO_REGISTER_SUCCESS)
    }
}