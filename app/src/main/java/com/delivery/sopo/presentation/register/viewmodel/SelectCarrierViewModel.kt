package com.delivery.sopo.presentation.register.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.data.database.room.entity.CarrierEntity
import com.delivery.sopo.data.models.Carrier
import com.delivery.sopo.data.models.Result
import com.delivery.sopo.data.repositories.local.repository.CarrierDataSource
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.presentation.views.adapter.SelectCarrier
import com.delivery.sopo.presentation.views.adapter.SelectType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectCarrierViewModel @Inject constructor(private val carrierDataSource: CarrierDataSource) :
    BaseViewModel() {
    val waybillNum = MutableLiveData<String>()
    val carrier = MutableLiveData<Carrier.Info>()

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String> get() = _navigator

    init {
        waybillNum.value = ""
    }

    fun postNavigator(navigator: String) {
        _navigator.postValue(navigator)
    }

    suspend fun getCarriers(): StateFlow<Result<List<Carrier.Info>>> = carrierDataSource.getAllCarriers().stateIn(scope = viewModelScope,)


    fun onClearClicked() {
        _navigator.value = NavigatorConst.REGISTER_INPUT_INFO
    }

    override fun onCleared() {
        super.onCleared()
    }

}