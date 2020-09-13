package com.delivery.sopo.viewmodels.registesrs

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.models.CourierItem
import com.delivery.sopo.util.fun_util.SingleLiveEvent

class RegisterStep3ViewModel : ViewModel()
{
    var waybilNum = MutableLiveData<String>()
    var courier = MutableLiveData<CourierItem>()

    var alias = MutableLiveData<String>()

    val isRevise = SingleLiveEvent<Boolean>()

    init
    {
//        isRevise.value = false
    }

    fun onReviseClicked()
    {
        isRevise.value = true
    }
}