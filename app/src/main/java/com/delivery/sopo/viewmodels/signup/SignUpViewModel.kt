package com.delivery.sopo.viewmodels.signup

import android.text.TextUtils
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.SuccessResult
import com.delivery.sopo.models.Result
import com.delivery.sopo.networks.handler.JoinHandler
import com.delivery.sopo.networks.repository.JoinRepository
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.views.widget.CustomEditText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

typealias FocusChangeCallback = (String, Boolean) -> Unit

class SignUpViewModel : ViewModel()
{
    val TAG = this.javaClass.simpleName

    var email = MutableLiveData<String>()
    var pwd = MutableLiveData<String>()
    var rePwd = MutableLiveData<String>()

    // 유효성 검사 시 틀릴 경우 EditText 상단 에러 메시지
    var emailValidateText = MutableLiveData<String>()
    var pwdValidateText = MutableLiveData<String>()
    var rePwdValidateText = MutableLiveData<String>()

    // 유효성 에러 가시성
    var isEmailErrorVisible = MutableLiveData<Int>()
    var isPwdErrorVisible = MutableLiveData<Int>()
    var isRePwdErrorVisible = MutableLiveData<Int>()

    // 유효성 옳음 가시성
    var isEmailCorVisible = MutableLiveData<Int>()
    var isPwdCorVisible = MutableLiveData<Int>()
    var isRePwdCorVisible = MutableLiveData<Int>()

    var emailStatusType = MutableLiveData<Int>()
    var pwdStatusType = MutableLiveData<Int>()
    var rePwdStatusType = MutableLiveData<Int>()

    var isAgree = MutableLiveData<Boolean>()

    var isDuplicate = true

    /**
     * 유효성 및 통신 등의 결과 객체
     */
    private var _result = MutableLiveData<Result<*, *>?>()
    val result: LiveData<Result<*, *>?>
        get() = _result

    val isProgress = MutableLiveData<Boolean?>()

    /**
     * 처리에 대한 결과 세팅
     */
    fun <T, E> postResultValue(
        successResult: SuccessResult<T>? = null,
        errorResult: ErrorResult<E>? = null
    ) = _result.postValue(Result(successResult, errorResult))

    /**
     * Progress turn on/off 비동기 value
     */
    fun postProgressValue(value: Boolean?) = isProgress.postValue(value)

    init
    {
        setInitValue()
    }

    private fun setInitValue()
    {
        email.value = ""
        pwd.value = ""
        rePwd.value = ""

        isEmailErrorVisible.value = GONE
        isPwdErrorVisible.value = GONE
        isRePwdErrorVisible.value = GONE

        isEmailCorVisible.value = GONE
        isPwdCorVisible.value = GONE
        isRePwdCorVisible.value = GONE

        emailValidateText.value = "이메일을 입력해주세요."
        pwdValidateText.value = "비밀번호를 입력해주세요."
        rePwdValidateText.value = "비밀번호 확인을 입력해주세요."

        emailStatusType.value = CustomEditText.STATUS_COLOR_ELSE
        pwdStatusType.value = CustomEditText.STATUS_COLOR_ELSE
        rePwdStatusType.value = CustomEditText.STATUS_COLOR_ELSE

        isAgree.value = false
    }

    // 개인정보 동의 이벤트
    fun onAgreeClicked() { isAgree.value = !isAgree.value!! }

    fun onSignUpClicked(v: View)
    {
        _result.value = checkValidate()

        if (_result.value!!.successResult != null)
        {
            JoinHandler.requestJoinBySelf(email = email.value!!, password = pwd.value!!, deviceInfo = SOPOApp.deviceInfo) { success, error ->
                postResultValue(success, error)
            }
        }
        else
        {
            val type = if (_result.value!!.errorResult!!.errorType == ErrorResult.ERROR_TYPE_DIALOG)
            {
                ErrorResult.ERROR_TYPE_DIALOG
            }
            else
            {
                ErrorResult.ERROR_TYPE_TOAST
            }

            _result.value!!.errorResult!!.errorType = type
            val tmp = _result.value
            _result.value = tmp
        }

        SopoLog.d(tag = TAG, msg = "SignUp Click!!!!!!!!!!!!!")
    }

    // EditText 유효성 검사 가시성
    private fun setVisibleState(type: String, errorState: Int, corState: Int)
    {
        when (type)
        {
            InfoConst.EMAIL ->
            {
                isEmailErrorVisible.value = (errorState)
                isEmailCorVisible.value = (corState)
            }
            InfoConst.PASSWORD ->
            {
                isPwdErrorVisible.value = (errorState)
                isPwdCorVisible.value = (corState)
            }
            InfoConst.RE_PASSWORD ->
            {
                isRePwdErrorVisible.value = (errorState)
                isRePwdCorVisible.value = (corState)
            }
        }
    }

    // CustomEditText Focus 제어
    var callback: FocusChangeCallback = FocusChangeCallback@{ type, focus ->

        if (focus)
        {
            setVisibleState(type = type, errorState = GONE, corState = GONE)

            isDuplicate = type == InfoConst.EMAIL

            when (type)
            {
                InfoConst.EMAIL -> emailStatusType.value = CustomEditText.STATUS_COLOR_BLUE
                InfoConst.PASSWORD -> pwdStatusType.value = CustomEditText.STATUS_COLOR_BLUE
                InfoConst.RE_PASSWORD -> rePwdStatusType.value = CustomEditText.STATUS_COLOR_BLUE
            }
        }
        else
        {
            when (type)
            {
                InfoConst.EMAIL ->
                {
                    // 이메일 란이 공백일 때
                    if (TextUtils.isEmpty(email.value))
                    {
                        emailStatusType.value = CustomEditText.STATUS_COLOR_RED
                        emailValidateText.value = "이메일을 입력해주세요."
                        setVisibleState(type = type, errorState = VISIBLE, corState = GONE)
                        _result.value = checkValidate()
                        SopoLog.d(tag = TAG, msg = "Email is Empty")
                        return@FocusChangeCallback
                    }

                    // 이메일 유효성 검사
                    val isValidate = ValidateUtil.isValidateEmail(email = email.value)

                    if (isValidate)
                    {
                        // 이메일이 중복 이메일이거나 초기화되었을 때 중복 api로 통신
                        if (isDuplicate)
                        {
                            SopoLog.d(tag = TAG, msg = "Duplicate Check Start!!!")
                            checkDuplicatedEmail(email = email.value!!)
                            return@FocusChangeCallback
                        }
                        else
                        {
                            emailStatusType.value = CustomEditText.STATUS_COLOR_BLUE
                            setVisibleState(
                                type = InfoConst.EMAIL,
                                errorState = GONE,
                                corState = VISIBLE
                            )
                        }
                    }
                    else
                    {
                        emailStatusType.value = 0
                        SopoLog.d(tag = TAG, msg = "PLZ Check Email Validate")
                        emailValidateText.value = "이메일 형식을 확인해주세요."
                        setVisibleState(type = type, errorState = VISIBLE, corState = GONE)
                    }

                    // 이메일, 비밀번호, 비밀번호 확인, 이용약관 유효성 검사
                    _result.value = checkValidate()

                    return@FocusChangeCallback
                }
                InfoConst.PASSWORD ->
                {
                    SopoLog.d(tag = TAG, msg = "Focus out $type ${pwd.value}")

                    // 비밀번호 란이 공백일 때
                    if (TextUtils.isEmpty(pwd.value))
                    {
                        pwdStatusType.value = CustomEditText.STATUS_COLOR_RED

                        SopoLog.d(tag = TAG, msg = "pwd is empty")

                        pwdValidateText.value = "비밀번호를 입력해주세요."
                        setVisibleState(type = type, errorState = VISIBLE, corState = GONE)
                        _result.value = checkValidate()

                        return@FocusChangeCallback
                    }

                    // 비밀번호 유효성 검사
                    val isValidate = ValidateUtil.isValidatePassword(pwd = pwd.value)

                    if (isValidate)
                    {
                        pwdStatusType.value = CustomEditText.STATUS_COLOR_BLUE
                        setVisibleState(type = type, errorState = GONE, corState = VISIBLE)

                        // 유효성 통과 시 비밀번호 확인 체크
                        if (pwd.value == rePwd.value)
                        {
                            pwdStatusType.value = CustomEditText.STATUS_COLOR_BLUE
                            rePwdStatusType.value = CustomEditText.STATUS_COLOR_BLUE
                            setVisibleState(InfoConst.RE_PASSWORD, GONE, VISIBLE)
                        }
                    }
                    else
                    {
                        pwdStatusType.value = CustomEditText.STATUS_COLOR_RED
                        pwdValidateText.value = "비밀번호 형식을 확인해주세요."
                        setVisibleState(type, VISIBLE, GONE)

                        // 비밀번호 일치 검사
                        if (rePwd.value!!.isNotEmpty())
                        {
                            rePwdStatusType.value = CustomEditText.STATUS_COLOR_RED
                            rePwdValidateText.value = "비밀번호가 일치하지 않습니다."
                            setVisibleState(InfoConst.RE_PASSWORD, VISIBLE, GONE)
                        }
                    }

                    _result.value = checkValidate()

                    return@FocusChangeCallback
                }
                InfoConst.RE_PASSWORD ->
                {
                    SopoLog.d(tag = TAG, msg = "type $type re_pwd ${rePwd.value}")

                    // 비밀번호가 최소 1자리 이상일 때
                    if (pwd.value!!.isNotEmpty())
                    {
                        // 비밀번호 확인 란이 공백일 때
                        if (TextUtils.isEmpty(rePwd.value))
                        {
                            rePwdStatusType.value = CustomEditText.STATUS_COLOR_RED
                            rePwdValidateText.value = "비밀번호 확인을 입력해주세요."
                            setVisibleState(type = type, errorState = VISIBLE, corState = GONE)
                            _result.value = checkValidate()

                            return@FocusChangeCallback

                        }

                        val isPwdValidate = ValidateUtil.isValidatePassword(pwd = pwd.value)
                        val isRePwdValidate = ValidateUtil.isValidatePassword(pwd = rePwd.value)

                        if (isPwdValidate && pwd.value == rePwd.value)
                        {
                            rePwdStatusType.value = CustomEditText.STATUS_COLOR_BLUE
                            // 비밀번호 유효성이 true일 때 비밀번호가 일치하는지
                            setVisibleState(InfoConst.RE_PASSWORD, GONE, VISIBLE)
                        }
                        else if (!isRePwdValidate)
                        {
                            pwdStatusType.value = if(isPwdValidate) CustomEditText.STATUS_COLOR_BLUE else CustomEditText.STATUS_COLOR_RED

                            rePwdStatusType.value = CustomEditText.STATUS_COLOR_RED
                            // 비밀번호 확인의 유효성이 false 일 때
                            rePwdValidateText.value = "비밀번호 형식을 확인해주세요."
                            setVisibleState(InfoConst.RE_PASSWORD, VISIBLE, GONE)
                        }
                        else
                        {
                            pwdStatusType.value = CustomEditText.STATUS_COLOR_RED
                            rePwdStatusType.value = CustomEditText.STATUS_COLOR_RED
                            // 비밀번호가 일치하지 않을 때
                            rePwdValidateText.value = "비밀번호가 일치하지 않습니다."
                            setVisibleState(InfoConst.RE_PASSWORD, VISIBLE, GONE)
                        }
                    }
                    else
                    {
                        val isRePwdValidate = ValidateUtil.isValidatePassword(pwd = rePwd.value)

                        if (!TextUtils.isEmpty(rePwd.value) && !isRePwdValidate)
                        {
                            rePwdStatusType.value = 0
                            // 비밀번호 확인의 유효성이 false일 때
                            rePwdValidateText.value = "비밀번호 확인의 형식을 확인해주세요."
                            setVisibleState(InfoConst.RE_PASSWORD, VISIBLE, GONE)
                        }
                        else if (isRePwdValidate)
                        {
                            pwdStatusType.value = 1
                            rePwdStatusType.value = 0
                            // 비밀번호 확인의 유효성이 true, 비밀번호 란이 공백일 때
                            rePwdValidateText.value = "첫번째 비밀번호를 입력해주세요."
                            setVisibleState(InfoConst.RE_PASSWORD, VISIBLE, GONE)
                        }
                    }

                    _result.value = checkValidate()

                    return@FocusChangeCallback
                }
            }
        }

    }

    private fun checkValidate(): Result<*, *>
    {
        SopoLog.d(msg = "checkValidate call()")
        // email 유효성 에러
        if (isEmailCorVisible.value != VISIBLE)
        {
            SopoLog.d(tag = TAG, msg = "Validate Fail Email ")
            emailStatusType.value = CustomEditText.STATUS_COLOR_RED
            return Result<Unit, Unit>(
                errorResult = ErrorResult(
                    code = null, errorMsg = emailValidateText.value.toString(), errorType = ErrorResult.ERROR_TYPE_NON, data = null
                )
            )
        }

        // password 유효성 에러
        if (isPwdCorVisible.value != VISIBLE)
        {
            SopoLog.d(tag = TAG, msg = "Validate Fail PWD ")
            pwdStatusType.value = CustomEditText.STATUS_COLOR_RED
            return Result<Unit, Unit>(
                errorResult = ErrorResult(
                    code = null, errorMsg = pwdValidateText.value.toString(), errorType = ErrorResult.ERROR_TYPE_NON, data = null
                )
            )
        }

        if (isRePwdCorVisible.value != VISIBLE)
        {
            rePwdStatusType.value = CustomEditText.STATUS_COLOR_RED
            SopoLog.d(tag = TAG, msg = "Validate Fail PWD ")

            return Result<Unit, Unit>(
                errorResult = ErrorResult(
                    code = null, errorMsg = rePwdValidateText.value.toString(), errorType = ErrorResult.ERROR_TYPE_NON, data = null
                )
            )
        }

        if (!isAgree.value!!)
        {
            SopoLog.d(tag = TAG, msg = "Validate Fail Agree ")

            return Result<Unit, Unit>(
                errorResult = ErrorResult(
                    code = null, errorMsg = "사용자 약관 이용에 동의해주세요!!!", errorType = ErrorResult.ERROR_TYPE_NON, data = null
                )
            )
        }

        SopoLog.d(tag = TAG, msg = "Validate Success ")

        return Result<Unit?, Unit?>(
            successResult = SuccessResult(
                code = null, successMsg = "SUCCESS", data = null
            )
        )
    }

    private fun checkDuplicatedEmail(email: String)
    {
        CoroutineScope(Dispatchers.IO).launch {

            JoinRepository().requestDuplicatedEmail(email = email){ success, error ->
                isDuplicate = if(success != null) success.data?:true else error?.data?:true

                val statusColor = if(isDuplicate) CustomEditText.STATUS_COLOR_RED else CustomEditText.STATUS_COLOR_BLUE
                val msg = if(isDuplicate)
                {
                    if(error != null) "알 수 없는 오류입니다." else "중복된 이메일입니다."
                } else ""

                emailStatusType.value = statusColor
                emailValidateText.value = msg

                setVisibleState(
                    type = InfoConst.EMAIL,
                    errorState = if(isDuplicate) VISIBLE else  GONE,
                    corState = if(isDuplicate) GONE else VISIBLE
                )
            }
        }
    }
}