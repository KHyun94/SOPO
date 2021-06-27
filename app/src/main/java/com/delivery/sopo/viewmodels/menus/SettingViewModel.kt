package com.delivery.sopo.viewmodels.menus

import android.view.View
import androidx.lifecycle.*
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.local.app_password.AppPasswordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SettingViewModel(private val appPasswordRepo: AppPasswordRepository) : ViewModel()
{
    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
    get() = _navigator

    private val _isSetOfSecurity = appPasswordRepo.getCntOfAppPasswordLiveData()
    val isSetOfSecurity: LiveData<Int>
        get() = _isSetOfSecurity

    private val _showSetPassword = MutableLiveData<Boolean>()
    val showSetPassword: LiveData<Boolean>
        get() = _showSetPassword

    fun setAppPassword(){
        _isSetOfSecurity.value?.also {
            if(it>0){
                viewModelScope.launch(Dispatchers.IO){
                    appPasswordRepo.deleteAll()
                }
            }
            else{
                _showSetPassword.value = true
            }
        }
    }

    fun onMoveToSetTimeClicked(v: View){
        _navigator.postValue(NavigatorConst.TO_NOT_DISTURB)
    }

    fun onSetNotifyOptionClicked(){
        _navigator.postValue(NavigatorConst.TO_SET_NOTIFY_OPTION)
    }

}