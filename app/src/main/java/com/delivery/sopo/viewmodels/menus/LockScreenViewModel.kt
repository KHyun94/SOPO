package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.enums.LockScreenStatus
import com.delivery.sopo.extensions.asSHA256
import com.delivery.sopo.database.room.entity.AppPasswordEntity
import com.delivery.sopo.repository.impl.AppPasswordRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LockScreenViewModel(
    private val userRepoImpl: UserRepoImpl,
    private val appPasswordRepo: AppPasswordRepoImpl) : ViewModel()
{
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"

    var lockPassword = MutableLiveData<String>()
    var firstCheck = MutableLiveData<Boolean>()
    var finalCheck = MutableLiveData<Boolean>()
    var passwordIsNotMatched = MutableLiveData<Boolean>()
    var isFinish = MutableLiveData<Boolean>()
    var verifyCnt = MutableLiveData<Int>()

    private val _lockScreenStatus = MutableLiveData<LockScreenStatus>()
    val lockScreenStatus: LiveData<LockScreenStatus>
        get() = _lockScreenStatus

    private val _verifyResult = MutableLiveData<Boolean>()
    val verifyResult: LiveData<Boolean>
        get() = _verifyResult

    private var firstCheckPassword = ""

    init{
        lockPassword.value = ""
        firstCheck.value = false
        finalCheck.value = false
        passwordIsNotMatched.value = false
        isFinish.value = false
        verifyCnt.value = 0
    }

    fun setLockScreenStatus(status: LockScreenStatus){
        _lockScreenStatus.value = status
    }

    private fun verifyPassword(inputPassword: String): Boolean{
        return appPasswordRepo.getAppPassword()?.let{
            it.appPassword == inputPassword.asSHA256
        } ?: false
    }

    fun lockPasswordAction(num: Int){
        when(_lockScreenStatus.value){
            LockScreenStatus.SET -> {
                setLockPassword(num)
            }
            LockScreenStatus.VERIFY -> {
                verifyLockPassword(num)
            }
        }
    }

    private fun verifyLockPassword(num: Int){
        lockPassword.value?.also {
            if (it.length < 4){
                lockPassword.value = "${it}$num"
            }
        }
        lockPassword.value?.also {
            if(it.length == 4){
                lockPassword.value = ""
                viewModelScope.launch(Dispatchers.IO) {
                    _verifyResult.postValue(verifyPassword(it))
                }
            }
        }
    }

    private fun setLockPassword(num: Int){

        lockPassword.value?.also {
            if (it.length < 4){
                lockPassword.value = "${it}$num"
            }
        }
        lockPassword.value?.also {
            if (it.length == 4 && firstCheckPassword == ""){
                firstCheckPassword = it
                lockPassword.value = ""
                verifyCnt.value = 1
            }
            else if(it.length == 4){

                //[다시 한번 입력해주세요.]에서 틀렸을 경우, 첨부터 다시 시도.
               if(firstCheckPassword != it){
                    lockPassword.value = ""
                    firstCheckPassword = ""
                    verifyCnt.value = 2
                }
                //[다시 한번 입력해주세요.]에서 일치한 경우, 저장 후 종료.
                else{
                   verifyCnt.value = 3
                   viewModelScope.launch(Dispatchers.IO){
                       appPasswordRepo.insertEntity(
                           AppPasswordEntity(
                           userName = userRepoImpl.getEmail(),
                           appPassword = lockPassword.value.toString().asSHA256
                       )
                       )
                   }
                }
            }
        }
    }

    fun eraseLockPassword(){
        val currentPassword = lockPassword.value

        currentPassword?.let {
            if(it.isNotEmpty()){
                val substring = it.substring(IntRange(0, (it.lastIndex-1)))
                lockPassword.value = substring
            }
        }
    }
}