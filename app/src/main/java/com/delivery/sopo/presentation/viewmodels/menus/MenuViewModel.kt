package com.delivery.sopo.presentation.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.data.repositories.local.user.UserLocalRepository
import com.delivery.sopo.enums.PersonalMessageEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.models.PersonalMessage
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog

class MenuViewModel(private val userLocalRepo: UserLocalRepository) : BaseViewModel()
{
    val nickname = MutableLiveData<String>().apply {
        val _nickname = userLocalRepo.getNickname()
        SopoLog.d("닉네임 >>> $_nickname")
        return@apply postValue(_nickname)
    }

    val personalMessage = MutableLiveData<PersonalMessage>().apply {
        val personalMessage = PersonalMessage(userLocalRepo.getPersonalStatusMessage(), userLocalRepo.getPersonalStatusType())
       return@apply postValue(personalMessage)
    }

    val personalMessageEnum = MutableLiveData<PersonalMessageEnum>().apply {
        val personalMessage = PersonalMessage(userLocalRepo.getPersonalStatusMessage(), userLocalRepo.getPersonalStatusType())
        return@apply postValue(personalMessage.personalMessageEnum)
    }

    private val _menu = MutableLiveData<TabCode>()
    val menu: LiveData<TabCode>
    get() = _menu

    fun onMoveToSubMenuClicked(code: TabCode){
        SopoLog.d("onMoveToSubMenuClicked() 호출 [TabCode:$code]")
        _menu.postValue(code)
    }
}