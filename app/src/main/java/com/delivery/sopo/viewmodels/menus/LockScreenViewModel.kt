package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.consts.LockStatusConst
import com.delivery.sopo.data.repository.database.room.dto.AppPasswordDTO
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.extensions.asSHA256
import com.delivery.sopo.data.repository.local.app_password.AppPasswordRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LockScreenViewModel(private val userLocalRepository: UserLocalRepository, private val appPasswordRepo: AppPasswordRepository): ViewModel()
{
    // SET,VERIFY,RESET: 현재 입력 받고 있는 비밀번호
    var inputLockNum = MutableLiveData<String>()

    // SET: 1차 입력 & 2차 입력 여부

    var isFinish = MutableLiveData<Boolean>()

    //
    var verifyType = MutableLiveData<String>()

    /** App 비밀번호 입력 타입
     * SET:비밀번호 설정
     *  1. 1차 검증(입력)
     *  2. 2치 검증(1차에 입력한 비밀번호와 일치하는지)
     *  3. 내부 DB에 저장
     */
    private val _lockScreenStatus = MutableLiveData<LockScreenStatusEnum>()
    val lockScreenStatusEnum: LiveData<LockScreenStatusEnum>
        get() = _lockScreenStatus

/*    private val _verifyResult = MutableLiveData<Boolean>()
    val verifyResult: LiveData<Boolean>
        get() = _verifyResult*/

    val pinCode = MutableLiveData<String?>()

    // SET: 1차 인증 번호 저장, 2차 입력 번
    private var primaryAuthNumber = ""

    init
    {
        inputLockNum.value = ""
        isFinish.value = false
        verifyType.value = ""
    }

    fun setLockScreenStatus(statusEnum: LockScreenStatusEnum)
    {
        _lockScreenStatus.value = statusEnum
    }

    private fun updateInputLockNum(num:Int):String{
        val beforeLockNum = inputLockNum.value ?: ""

        // 입력 전 키패드 숫자가 4자리 미만일 때
        if(beforeLockNum.length < 4)
        {
            inputLockNum.value = "$beforeLockNum$num"
            SopoLog.d("입력 후 번호 [data:${inputLockNum.value}]")
        }

        return inputLockNum.value ?: ""
    }

    private suspend fun verifyPassword(inputPassword: String):Boolean = withContext(Dispatchers.Default) {
        return@withContext appPasswordRepo.get()?.let {
            it.appPassword == inputPassword.asSHA256
        } ?: false
    }

    // email pin num compare
    private fun verifyPasswordByEmail(inputPassword: String): Boolean
    {
        if(pinCode.value == null) return false
        return pinCode.value == inputPassword
    }

    // 버튼 누르기 이벤트
    fun onPressLockKeyPad(num: Int)
    {
        val currentLockNum = updateInputLockNum(num).apply {
            if(length != 4) return
        }

        when(_lockScreenStatus.value)
        {
            LockScreenStatusEnum.SET -> setLockPassword(currentLockNum)
            LockScreenStatusEnum.VERIFY -> verifyLockPassword(currentLockNum)
            LockScreenStatusEnum.RESET_ACCOUNT_PASSWORD -> verifyAuthPinNumber(currentLockNum)
        }
    }

    private fun setLockPassword(lockNum: String)
    {
        SopoLog.i("setLockPassword(...) 호출 [data:$lockNum]")

        // 1차 입력
        if(primaryAuthNumber == "")
        {
            primaryAuthNumber = lockNum
            inputLockNum.value = ""
            verifyType.value = LockStatusConst.SET.VERIFY_STATUS
            return
        }

        // 2차 입력 실패
        if(primaryAuthNumber != lockNum)
        {
            inputLockNum.value = ""
            primaryAuthNumber = ""
            verifyType.value = LockStatusConst.SET.FAILURE_STATUS
            return
        }

        // 2차 입력 성공
        verifyType.value = LockStatusConst.SET.CONFIRM_STATUS

        viewModelScope.launch(Dispatchers.Default) {
            val dto = AppPasswordDTO(userId = userLocalRepository.getUserId(), appPassword = inputLockNum.value.toString().asSHA256, auditDte = TimeUtil.getDateTime())
            appPasswordRepo.insert(dto)
        }
    }

    private fun verifyLockPassword(lockNum: String)
    {
        inputLockNum.postValue("")

        viewModelScope.launch(Dispatchers.IO) {
            val isVerify = verifyPassword(lockNum)
            if(!isVerify) return@launch verifyType.postValue(LockStatusConst.VERIFY.FAILURE_STATUS)
            verifyType.postValue(LockStatusConst.VERIFY.CONFIRM_STATUS)
        }
    }

    private fun verifyAuthPinNumber(lockNum: String)
    {
        inputLockNum.postValue("")

        viewModelScope.launch(Dispatchers.IO) {
            val isVerify = verifyPasswordByEmail(lockNum)
            if(!isVerify) return@launch verifyType.postValue(LockStatusConst.VERIFY.FAILURE_STATUS)
            verifyType.postValue(LockStatusConst.VERIFY.CONFIRM_STATUS)
        }
    }
    
    fun eraseLockPassword()
    {
        val currentPassword = inputLockNum.value

        currentPassword?.let {
            if(it.isNotEmpty())
            {
                val substring = it.substring(IntRange(0, (it.lastIndex - 1)))
                inputLockNum.value = substring
            }
        }
    }
}
