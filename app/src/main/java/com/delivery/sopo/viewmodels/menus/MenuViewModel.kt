package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.models.PersonalMessage
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.livedates.SingleLiveEvent

class MenuViewModel(private val userLocalRepo: UserLocalRepository) : ViewModel()
{
    val personalMessage = MutableLiveData<PersonalMessage>().apply {
        val personalMessage = PersonalMessage(userLocalRepo.getPersonalStatusMessage(), userLocalRepo.getPersonalStatusType())

        SopoLog.d("data!!!! >>> ${personalMessage.toString()}")

        return@apply postValue(personalMessage)
    }

    private val _code = MutableLiveData<TabCode>()
    val menu: LiveData<TabCode>
    get() = _code

    fun onMoveToSubMenuClicked(code: TabCode){
        SopoLog.d("onMoveToSubMenuClicked() 호출 [TabCode:$code]")
        _code.postValue(code)
    }

    override fun onCleared()
    {
        super.onCleared()
    }
}