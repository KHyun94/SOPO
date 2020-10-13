package com.delivery.sopo.viewmodels.menus

import android.util.Log
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.enums.MenuEnum
import com.delivery.sopo.extensions.MutableLiveDataExtension.popItem
import com.delivery.sopo.extensions.MutableLiveDataExtension.pushItem
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.TimeCountRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
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

    private val _menu = MutableLiveData<MenuEnum>()
    val menu: LiveData<MenuEnum>
        get() = _menu

    private val _viewStack = MutableLiveData<Stack<MenuEnum>>()
    val viewStack: LiveData<Stack<MenuEnum>>
        get() = _viewStack

    init {
        _userEmail.value = userRepoImpl.getEmail()
        _viewStack.value = Stack()
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
            Log.e(TAG, "STACK IS ALREADY EMPTY!!, you try to pop item even if stack is already empty!!")
            false
        }
    }
}