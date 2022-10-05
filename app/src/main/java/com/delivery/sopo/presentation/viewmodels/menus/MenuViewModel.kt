package com.delivery.sopo.presentation.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.data.resources.user.local.UserDataSource
import com.delivery.sopo.enums.PersonalMessageEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.models.PersonalMessage
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(private val userDataSource: UserDataSource) : BaseViewModel()
{
    val nickname = MutableLiveData<String>()

    val personalMessage = MutableLiveData<PersonalMessage>()
    val personalMessageEnum = MutableLiveData<PersonalMessageEnum>()

    private val _menu = MutableLiveData<TabCode>()
    val menu: LiveData<TabCode>
    get() = _menu

    init
    {
        postNickname()
        postPersonalMessage()
    }

    fun postNickname() = viewModelScope.launch{
        nickname.postValue(userDataSource.getNickname())
    }

    fun postPersonalMessage() = viewModelScope.launch {
        val _personalMessage = PersonalMessage(userDataSource.getPersonalStatusMessage(), userDataSource.getPersonalStatusType())
        personalMessageEnum.postValue(_personalMessage.personalMessageEnum)
        personalMessage.postValue(_personalMessage)
    }

    fun onMoveToSubMenuClicked(code: TabCode){
        SopoLog.d("onMoveToSubMenuClicked() 호출 [TabCode:$code]")
        _menu.postValue(code)
    }
}