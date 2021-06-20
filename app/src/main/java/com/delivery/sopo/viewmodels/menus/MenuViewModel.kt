package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.enums.MenuEnum
import com.delivery.sopo.extensions.MutableLiveDataExtension.popItem
import com.delivery.sopo.extensions.MutableLiveDataExtension.pushItem
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MenuViewModel(private val userLocalRepository: UserLocalRepository
) : ViewModel(), LifecycleObserver
{
    private val _menu = MutableLiveData<TabCode>()
    val menu: LiveData<TabCode>
        get() = _menu

    fun onMoveToSubMenuClicked(code: TabCode){
        _menu.postValue(code)
    }
}