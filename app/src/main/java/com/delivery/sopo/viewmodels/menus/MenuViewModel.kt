package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.models.PersonalMessage
import com.delivery.sopo.util.SopoLog

class MenuViewModel(private val userLocalRepo: UserLocalRepository) : ViewModel()
{
    val personalMessage = MutableLiveData<PersonalMessage>().apply {

//        val personalMessage = PersonalMessage(userLocalRepo.getPersonalStatusMessage(), userLocalRepo.getPersonalStatusType())
        val personalMessage = PersonalMessage("SOPO와 함께한 택배\n오늘로 21개 째", 5)
       return@apply postValue(personalMessage)
    }

    private val _menu = MutableLiveData<TabCode>()
    val menu: LiveData<TabCode>
    get() = _menu

    fun onMoveToSubMenuClicked(code: TabCode){
        SopoLog.d("onMoveToSubMenuClicked() 호출 [TabCode:$code]")
        _menu.postValue(code)
    }
}