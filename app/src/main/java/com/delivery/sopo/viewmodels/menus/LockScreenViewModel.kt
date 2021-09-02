package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.extensions.asSHA256
import com.delivery.sopo.data.repository.database.room.entity.AppPasswordEntity
import com.delivery.sopo.data.repository.local.app_password.AppPasswordRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LockScreenViewModel(private val userLocalRepository: UserLocalRepository, private val appPasswordRepo: AppPasswordRepository):
        ViewModel()
{

    var lockPassword = MutableLiveData<String>()
    var firstCheck = MutableLiveData<Boolean>()
    var finalCheck = MutableLiveData<Boolean>()
    var passwordIsNotMatched = MutableLiveData<Boolean>()
    var isFinish = MutableLiveData<Boolean>()
    var verifyCnt = MutableLiveData<Int>()

    private val _lockScreenStatus = MutableLiveData<LockScreenStatusEnum>()
    val lockScreenStatusEnum: LiveData<LockScreenStatusEnum>
        get() = _lockScreenStatus

    private val _verifyResult = MutableLiveData<Boolean>()
    val verifyResult: LiveData<Boolean>
        get() = _verifyResult

    val pinCode = MutableLiveData<String?>()

    private var firstCheckPassword = ""

    init
    {
        lockPassword.value = ""
        firstCheck.value = false
        finalCheck.value = false
        passwordIsNotMatched.value = false
        isFinish.value = false
        verifyCnt.value = 0
    }

    fun setLockScreenStatus(statusEnum: LockScreenStatusEnum)
    {
        _lockScreenStatus.value = statusEnum
    }

    private fun verifyPassword(inputPassword: String): Boolean
    {
        return appPasswordRepo.get()?.let {
            it.appPassword == inputPassword.asSHA256
        } ?: false
    }

    // email pin num compare
    private fun verifyPasswordByEmail(inputPassword: String): Boolean
    {
        if(pinCode.value == null) return false
        return pinCode.value == inputPassword
    }

    fun onPressLockKeyPad(num: Int)
    {
        when(_lockScreenStatus.value)
        {
            LockScreenStatusEnum.SET -> setLockPassword(num)
            LockScreenStatusEnum.VERIFY -> verifyLockPassword(num)
            LockScreenStatusEnum.RESET_ACCOUNT_PASSWORD -> verifyLockPassword(num)
        }
    }

    private fun setLockPassword(num: Int)
    {
        lockPassword.value?.also {
            if(it.length < 4)
            {
                lockPassword.value = "${it}$num"
            }
        }
        lockPassword.value?.also {
            if(it.length == 4 && firstCheckPassword == "")
            {
                firstCheckPassword = it
                lockPassword.value = ""
                verifyCnt.value = 1
            }
            else if(it.length == 4)
            {

                //[다시 한번 입력해주세요.]에서 틀렸을 경우, 첨부터 다시 시도.
                if(firstCheckPassword != it)
                {
                    lockPassword.value = ""
                    firstCheckPassword = ""
                    verifyCnt.value = 2
                }
                //[다시 한번 입력해주세요.]에서 일치한 경우, 저장 후 종료.
                else
                {
                    verifyCnt.value = 3
                    viewModelScope.launch(Dispatchers.IO) {
                        appPasswordRepo.insert(
                            AppPasswordEntity(userId = userLocalRepository.getUserId(),
                                              appPassword = lockPassword.value.toString().asSHA256))
                    }
                }
            }
        }
    }

    private fun verifyLockPassword(num: Int)
    {
        lockPassword.value?.also {
            if(it.length < 4)
            {
                lockPassword.value = "${it}$num"
            }
        }
        lockPassword.value?.also {
            if(it.length == 4)
            {
                lockPassword.value = ""
                viewModelScope.launch(Dispatchers.IO) {

                    val isVerify = when(lockScreenStatusEnum.value)
                    {
                        LockScreenStatusEnum.RESET_ACCOUNT_PASSWORD ->
                        {
                            verifyPasswordByEmail(it)
                        }
                        else ->
                        {
                            verifyPassword(it)
                        }
                    }

                    SopoLog.d("비밀번호 재설정 >>> $it, 성공 여부 >>> $isVerify")

                    _verifyResult.postValue(isVerify)
                }
            }
        }
    }

    fun eraseLockPassword()
    {
        val currentPassword = lockPassword.value

        currentPassword?.let {
            if(it.isNotEmpty())
            {
                val substring = it.substring(IntRange(0, (it.lastIndex - 1)))
                lockPassword.value = substring
            }
        }
    }
}
