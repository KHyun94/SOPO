package com.delivery.sopo.presentation.viewmodels.menus

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.consts.LockStatusConst
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.data.database.room.dto.AppPasswordDTO
import com.delivery.sopo.data.repositories.local.app_password.AppPasswordRepository
import com.delivery.sopo.data.resources.user.local.UserDataSource
import com.delivery.sopo.enums.LockScreenStatusEnum
import com.delivery.sopo.extensions.asSHA256
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.TimeUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class LockScreenViewModel @Inject constructor(private val userDataSource: UserDataSource, private val appPasswordRepo: AppPasswordRepository):
        BaseViewModel()
{
    var title = MutableLiveData<String>()

    // SET,VERIFY,RESET: 현재 입력 받고 있는 비밀번호
    var lockNum = MutableLiveData<String>()

    private val _lockScreenStatus = MutableLiveData<LockScreenStatusEnum>()
    val lockScreenStatus: LiveData<LockScreenStatusEnum>
        get() = _lockScreenStatus

    var verifyType = MutableLiveData<String>()

    private val _error = MutableLiveData<String>()
    val error: LiveData<String>
        get() = _error

    var authCode: String = ""

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    fun postNavigator(navigator: String)
    {
        _navigator.postValue(navigator)
    }

    fun onBackClicked(){
        postNavigator(NavigatorConst.Event.BACK)
    }

    // 1차 인증 번호 저장
    private var primaryAuthNumber = ""

    var isButtonEnabled = MutableLiveData<Boolean>()

    init
    {
        lockNum.value = ""
        verifyType.value = ""
    }

    fun setLockScreenStatus(status: LockScreenStatusEnum)
    {
        _lockScreenStatus.postValue(status)
    }

    // 입력 데이터가 들어온 값을 기존 입력 데이터에 병합
    private fun updateInputLockNum(num: Int): String
    {
        val beforeLockNum = lockNum.value ?: ""

        if(beforeLockNum.length < 4) lockNum.value = "$beforeLockNum$num"

        return lockNum.value ?: ""
    }

    // VERIFY: 내부 DB에 저장된 '앱 잠금 번호'와 비교(SHA-256 암호화)
    private suspend fun verifyPassword(inputPassword: String): Boolean =
        withContext(Dispatchers.Default) {
            return@withContext appPasswordRepo.get()?.let {
                it.appPassword == inputPassword.asSHA256
            } ?: false
        }

    // 버튼 누르기 이벤트
    fun onPressLockKeyPadClicked(num: Int)
    { // 완성된 입력 데이터가 4글자를 넘지 않으면 반환
        val currentLockNum = updateInputLockNum(num).apply {
            if(length != 4) return
        }

        // 완성된 입력 데이터가 4글자 일 때 각 status 기준으로 비교
        when(lockScreenStatus.value)
        {
            LockScreenStatusEnum.SET_CONFIRM -> checkLockPassword(currentLockNum)
            LockScreenStatusEnum.SET_UPDATE -> setLockPassword(currentLockNum)
            LockScreenStatusEnum.VERIFY -> verifyLockPassword(currentLockNum)
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
            val dto =
                AppPasswordDTO(userId = userDataSource.getUsername(), appPassword = lockNum.asSHA256, auditDte = TimeUtil.getDateTime())
            appPasswordRepo.insert(dto)
        }
    }

    // SET_CONFIRM: 내부 DB에 저장된
    private fun checkLockPassword(lockNum: String) = scope.launch(Dispatchers.Default) {
        SopoLog.i("checkLockPassword(...) 호출 [data:$lockNum]")

        val appPassword = appPasswordRepo.get()

        if(appPassword?.appPassword != lockNum.asSHA256)
        {
            this@LockScreenViewModel.lockNum.postValue("")
            verifyType.postValue(LockStatusConst.CONFIRM.FAILURE_STATUS)
            return@launch
        }

        // 입력 데이터 초기화
        this@LockScreenViewModel.lockNum.postValue("")

        setLockScreenStatus(LockScreenStatusEnum.SET_UPDATE)
        // 1차 입력 데이터가 빈 값일 때 임시 저장 후, 2차 입력 상태로 변경
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
}
