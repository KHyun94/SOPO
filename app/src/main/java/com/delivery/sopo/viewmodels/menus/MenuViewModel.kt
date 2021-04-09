package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.enums.MenuEnum
import com.delivery.sopo.extensions.MutableLiveDataExtension.popItem
import com.delivery.sopo.extensions.MutableLiveDataExtension.pushItem
import com.delivery.sopo.models.SopoJsonPatch
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.UserAPI
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.networks.dto.JsonPatchDto
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.TimeCountRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*

class MenuViewModel(private val userRepoImpl: UserRepoImpl,
                    private val parcelRepoImpl: ParcelRepoImpl,
                    private val timeCountRepoImpl: TimeCountRepoImpl
) : ViewModel(), LifecycleObserver
{
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"

    private val _cntOfSoonListItem = parcelRepoImpl.getSoonDataCntLiveData()
    val cntOfSoonListItem: LiveData<Int>
        get() = _cntOfSoonListItem

    private val _cntOfOngoingListItem = parcelRepoImpl.getOngoingDataCntLiveData()
    val cntOfOngoingListItem: LiveData<Int>
        get() = _cntOfOngoingListItem

    private val _cntOfCompleteListItem = timeCountRepoImpl.getSumOfCountLiveData()
    val cntOfCompleteListItem: LiveData<Int>
        get() = _cntOfCompleteListItem

    private val _userEmail = MutableLiveData<String>()
    val userEmail: LiveData<String>
        get() = _userEmail
    //todo 닉네임 업데이트

    private val _userNickname  = MutableLiveData<String>()
    val userNickname : LiveData<String>
        get() = _userNickname

    private val _menu = MutableLiveData<MenuEnum>()
    val menu: LiveData<MenuEnum>
        get() = _menu

    private val _viewStack = MutableLiveData<Stack<MenuEnum>>()
    val viewStack: LiveData<Stack<MenuEnum>>
        get() = _viewStack

    val isUpdate = MutableLiveData<Boolean>()

    init {
        _userEmail.value = userRepoImpl.getEmail()
        _userNickname.value = userRepoImpl.getNickname()
        SopoLog.d(msg = "Menu 닉네임 => ${userNickname.value}")
        _viewStack.value = Stack()
        isUpdate.value = false
    }

    fun pushView(menu: MenuEnum){
        _viewStack.pushItem(menu)
        _menu.value = menu
    }

    fun popView(): Boolean{
        return try {
            _viewStack.popItem()
            _viewStack.value?.also {
                if(!it.empty()){
                    _menu.value = it.peek()
                }
            }
            true
        }
        catch (e: EmptyStackException){
            SopoLog.d(
                msg = "STACK IS ALREADY EMPTY!!, you try to pop item even if stack is already empty!!"
            )
            false
        }
    }

    fun onUpdateClicked()
    {
        SopoLog.d(msg = "닉네임 변경 클릭")
        isUpdate.value = true
    }

    fun updateUserNickname(nickname : String)
    {
        CoroutineScope(Dispatchers.IO).launch {

            when (val result = UserCall.updateNickname(nickname = nickname))
            {
                is NetworkResult.Success ->
                {
                    SopoLog.d( msg = "Success To Update Nickname ${result.data.message}")
                    _userNickname.postValue(nickname)
                }
                is NetworkResult.Error ->
                {
                    SopoLog.d( msg = "Fail To Update Nickname ${result.exception.message}")
                }
            }
        }
    }
}