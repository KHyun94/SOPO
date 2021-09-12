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
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.models.EmailAuthDTO
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LockScreenViewModel(
        private val userLocalRepo: UserLocalRepository,
        private val userRemoteRepo: UserRemoteRepository,
        private val appPasswordRepo: AppPasswordRepository
        ): ViewModel()
{
    // SET,VERIFY,RESET: 현재 입력 받고 있는 비밀번호
    var lockNum = MutableLiveData<String>()

    private val _lockScreenStatus = MutableLiveData<LockScreenStatusEnum>()
    val lockScreenStatus: LiveData<LockScreenStatusEnum>
        get() = _lockScreenStatus

    var verifyType = MutableLiveData<String>()

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    // 1차 인증 번호 저장
    private var primaryAuthNumber = ""

    // AUTH:
    // 비밀번호 재설정 상태에서만 사용하는 프로퍼티
    var pinCode: String
    var jwtToken: String
    var isActivateResendMail = MutableLiveData<Boolean>()

    var isButtonEnabled = MutableLiveData<Boolean>()

    // Pin num 오류 체크
    private var cntOfAuthError = 0

    init
    {
        pinCode = ""
        jwtToken = ""

        lockNum.value = ""

        verifyType.value = ""
    }

    fun setLockScreenStatus(status: LockScreenStatusEnum)
    {
        SopoLog.i("setLockScreenStatus(...) 호출 [status:$status]")
        _lockScreenStatus.value = status
    }

    // 입력 데이터가 들어온 값을 기존 입력 데이터에 병합
    private fun updateInputLockNum(num: Int): String
    {
        val beforeLockNum = lockNum.value ?: ""

        if(beforeLockNum.length < 4) lockNum.value = "$beforeLockNum$num"

        return lockNum.value ?: ""
    }

    // VERIFY: 내부 DB에 저장된 '앱 잠금 번호'와 비교(SHA-256 암호화)
    private suspend fun verifyPassword(inputPassword: String): Boolean = withContext(Dispatchers.Default) {
            return@withContext appPasswordRepo.get()?.let {
                it.appPassword == inputPassword.asSHA256
            } ?: false
        }

    // AUTH: 생성된 'PIN CODE'와 입력받은 'PIN CODE'를 비교
    private fun verifyPasswordByEmail(inputPassword: String): Boolean
    {
        if(pinCode == "") return false
        return pinCode == inputPassword
    }

    // 버튼 누르기 이벤트
    fun onPressLockKeyPadClicked(num: Int)
    {
        // 완성된 입력 데이터가 4글자를 넘지 않으면 반환
        val currentLockNum = updateInputLockNum(num).apply {
            if(length != 4) return
        }

        // 완성된 입력 데이터가 4글자 일 때 각 status 기준으로 비교
        when(lockScreenStatus.value)
        {
            LockScreenStatusEnum.SET -> setLockPassword(currentLockNum)
            LockScreenStatusEnum.VERIFY -> verifyLockPassword(currentLockNum)
            LockScreenStatusEnum.RESET_ACCOUNT_PASSWORD -> verifyAuthPinNumber(currentLockNum)
        }
    }

    // SET: 입력 데이터를 내부 DB에 저장
    private fun setLockPassword(lockNum: String)
    {
        SopoLog.i("setLockPassword(...) 호출 [data:$lockNum]")

        // 1차 입력 데이터가 빈 값일 때 임시 저장 후, 2차 입력 상태로 변경
        if(primaryAuthNumber == "")
        {
            primaryAuthNumber = lockNum
            this.lockNum.value = ""
            verifyType.value = LockStatusConst.SET.VERIFY_STATUS
            return
        }

        // 1, 2차 입력 데이터가 달랐을 때, 처음 상태로 이동
        if(primaryAuthNumber != lockNum)
        {
            this.lockNum.value = ""
            primaryAuthNumber = ""
            verifyType.value = LockStatusConst.SET.FAILURE_STATUS
            return
        }

        // 입력 데이터 초기화
        this.lockNum.postValue("")

        // 1, 2차 입력 데이터가 같을 때
        verifyType.value = LockStatusConst.SET.CONFIRM_STATUS

        viewModelScope.launch(Dispatchers.Default) {
            val dto = AppPasswordDTO(userId = userLocalRepo.getUserId(),
                                     appPassword = lockNum.asSHA256,
                                     auditDte = TimeUtil.getDateTime())
            appPasswordRepo.insert(dto)
        }
    }

    // VERIFY: 입력 데이터와 저장된 데이터가 동일한지 비교
    private fun verifyLockPassword(lockNum: String)
    {
        SopoLog.i("verifyLockPassword(...) 호출 [data:$lockNum]")

        // 입력 데이터 초기화
        this.lockNum.postValue("")

        viewModelScope.launch(Dispatchers.IO) {
            val isVerify = verifyPassword(lockNum)
            if(!isVerify) return@launch verifyType.postValue(LockStatusConst.VERIFY.FAILURE_STATUS)
            verifyType.postValue(LockStatusConst.VERIFY.CONFIRM_STATUS)
        }
    }

    // AUTH: 입력 데이터와 'PIN CODE'가 동일한지 비교
    private fun verifyAuthPinNumber(lockNum: String)
    {
        SopoLog.i("verifyAuthPinNumber(...) 호출 [data:$lockNum]")

        // 입력 데이터 초기화
        this.lockNum.postValue("")

        viewModelScope.launch(Dispatchers.IO) {
            val isVerify = verifyPasswordByEmail(lockNum)

            if(!isVerify) cntOfAuthError++

            SopoLog.d("틀린 횟수:$cntOfAuthError")

            if(cntOfAuthError == 2)
            {
                SopoLog.d("틀린 횟수가 2회임")
                isActivateResendMail.postValue(true)
            }

            if(!isVerify) return@launch verifyType.postValue(LockStatusConst.AUTH.FAILURE_STATUS)

            verifyType.postValue(LockStatusConst.AUTH.CONFIRM_STATUS)
        }
    }

    fun eraseLockPassword()
    {
        val currentPassword = lockNum.value

        currentPassword?.let {
            if(it.isNotEmpty())
            {
                val substring = it.substring(IntRange(0, (it.lastIndex - 1)))
                lockNum.value = substring
            }
        }
    }

    fun onResendAuthMailClicked()
    {
        SopoLog.i("onResendAuthMailClicked(...) 호출")

        isButtonEnabled.postValue(false)

        CoroutineScope(Dispatchers.Main).launch {


            val result:ResponseResult<EmailAuthDTO?> = withContext(Dispatchers.IO) {
                userRemoteRepo.requestEmailForAuth(userLocalRepo.getUserId())
            }

            // 인증 메일 보내기 실패했을 때, UI 상 재시도 요청
            if(!result.result)
            {
                isButtonEnabled.postValue(true)

                _error.postValue("인증 메일을 다시 보내는데 실패했습니다.")
                return@launch
            }


            if(result.data == null)
            {
                isButtonEnabled.postValue(true)

                _error.postValue("인증 메일을 다시 보내는데 실패했습니다.")
                return@launch
            }

            cntOfAuthError = 0

            pinCode = result.data.code
            jwtToken = result.data.token

            isActivateResendMail.postValue(false)
            isButtonEnabled.postValue(true)
        }
    }
}
