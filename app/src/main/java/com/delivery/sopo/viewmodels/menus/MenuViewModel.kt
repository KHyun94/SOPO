package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.exceptions.UserExceptionHandler
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.PersonalMessageEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.PersonalMessage
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers

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

    override var onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onFailure(error: ErrorEnum)
        { // TODO 발생하는 에러가 있을까?
            //            postErrorSnackBar("로그인에 실패했습니다.")
        }

        override fun onInternalServerError(error: ErrorEnum)
        {
            super.onInternalServerError(error)
            postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
        }
    }

    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        UserExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }
}