package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.consts.LockStatusConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.database.room.dto.AppPasswordDTO
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.extensions.asSHA256
import com.delivery.sopo.data.repository.local.app_password.AppPasswordRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.exceptions.UserExceptionHandler
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.user.ResetAuthCode
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.TimeUtil
import kotlinx.coroutines.*

class LockScreenViewModel(
        private val userLocalRepo: UserLocalRepository,
        private val userRemoteRepo: UserRemoteRepository,
        private val appPasswordRepo: AppPasswordRepository
        ): BaseViewModel()
{
    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    // SET,VERIFY,RESET: 현재 입력 받고 있는 비밀번호
    var lockNum = MutableLiveData<String>()

    private val _lockScreenStatus = MutableLiveData<LockScreenStatusEnum>()
    val lockScreenStatus: LiveData<LockScreenStatusEnum>
        get() = _lockScreenStatus

    var verifyType = MutableLiveData<String>()

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    private val onSOPOErrorCallback = object : OnSOPOErrorCallback
    {

        override fun onFailure(error: ErrorEnum)
        {
            stopLoading()
            cntOfAuthError++
            verifyType.postValue(LockStatusConst.AUTH.FAILURE_STATUS)
//            when(error)
//            {
//                ErrorEnum.VALIDATION ->
//                {
//                }
//                else ->
//                {
//                }
//            }
        }

        override fun onInternalServerError(error: ErrorEnum)
        {
            super.onInternalServerError(error)
            stopLoading()
            postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
        }

        override fun onAuthError(error: ErrorEnum)
        {
            super.onAuthError(error)
            stopLoading()
            postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
        }
    }

    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        UserExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }

    // 1차 인증 번호 저장
    private var primaryAuthNumber = ""

    // AUTH:
    var email: String = ""
    var jwtToken: String = ""
    var isActivateResendMail = MutableLiveData<Boolean>()

    var isButtonEnabled = MutableLiveData<Boolean>()

    // Pin num 오류 체크
    private var cntOfAuthError = 0

    init
    {
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
    private fun verifyPasswordByEmail(authCode: String) = scope.launch(Dispatchers.IO) {
        try
        {
            userRemoteRepo.requestVerifyAuthToken(ResetAuthCode(jwtToken, authCode, email))
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }
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

        startLoading()

        // 입력 데이터 초기화
        this.lockNum.postValue("")

        verifyPasswordByEmail(lockNum)

        stopLoading()

        viewModelScope.launch(Dispatchers.IO) {

            SopoLog.d("틀린 횟수:$cntOfAuthError")

            if(cntOfAuthError == 2)
            {
                SopoLog.d("틀린 횟수가 2회임")
                isActivateResendMail.postValue(true)
                return@launch
            }

//            if(!isVerify) return@launch

//            verifyType.postValue(LockStatusConst.AUTH.CONFIRM_STATUS)
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

        _navigator.postValue("CANCEL")
    }
}
