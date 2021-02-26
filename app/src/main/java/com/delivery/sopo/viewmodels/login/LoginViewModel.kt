package com.delivery.sopo.viewmodels.login

import android.text.TextUtils
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.models.*
import com.delivery.sopo.networks.handler.LoginHandler
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.viewmodels.signup.FocusChangeCallback
import com.delivery.sopo.views.widget.CustomEditText

class LoginViewModel(val userRepoImpl: UserRepoImpl) : ViewModel()
{
    private val TAG = "LOG.SOPO.LoginVm"

    var email = MutableLiveData<String>()
    var pwd = MutableLiveData<String>()
    var uid = ""

    // CustomEditText의 에러 텍스트
    var emailValidateText = MutableLiveData<String>()
    var pwdValidateText = MutableLiveData<String>()

    // CustomEditText의우측 상단 텍스트 visible
    var isEmailErrorVisible = MutableLiveData<Int>()
    var isPwdErrorVisible = MutableLiveData<Int>()

    // CustomEditText의유효성 검사 통과; 우측 이미지 visible
    var isEmailCorVisible = MutableLiveData<Int>()
    var isPwdCorVisible = MutableLiveData<Int>()

    // CustomEditText의 underline state
    var emailStatusType = MutableLiveData<Int>()
    var pwdStatusType = MutableLiveData<Int>()

    // 유효성 및 통신 등의 결과 객체
    private var _result = MutableLiveData<Result<*, *>?>()
    val result: LiveData<Result<*, *>?>
        get() = _result

    val isProgress = MutableLiveData<Boolean?>()

    fun <T, E> postResultValue(
        successResult: SuccessResult<T>? = null,
        errorResult: ErrorResult<E>? = null
    ) = _result.postValue(Result(successResult, errorResult))

    fun setProgressValue(value: Boolean?) = isProgress.postValue(value)

    init
    {
        setInitValue()
    }

    // UI 초기화
    private fun setInitValue()
    {
        email.value = ""
        pwd.value = ""

        isEmailErrorVisible.value = View.GONE
        isPwdErrorVisible.value = View.GONE

        isEmailCorVisible.value = View.GONE
        isPwdCorVisible.value = View.GONE

        emailValidateText.value = "이메일을 입력해주세요."
        pwdValidateText.value = "비밀번호를 입력해주세요."

        emailStatusType.value = CustomEditText.STATUS_COLOR_ELSE
        pwdStatusType.value = CustomEditText.STATUS_COLOR_ELSE
    }

    // CustomEditText의 success, error state 제어
    private fun setVisibleState(type: String, errorState: Int, corState: Int)
    {
        when (type)
        {
            InfoConst.EMAIL ->
            {
                isEmailErrorVisible.value = errorState
                isEmailCorVisible.value = corState

            }
            InfoConst.PASSWORD ->
            {
                isPwdErrorVisible.value = errorState
                isPwdCorVisible.value = corState
            }
        }
    }

    // CustomEditText Focus 제어
    var callback: FocusChangeCallback = FocusChangeCallback@{ type, focus ->

        if (focus)
        {
            // success mark, error text Visible -> GONE
            setVisibleState(type = type, errorState = View.GONE, corState = View.GONE)

            // 선택한 CustomEditText의 underline 색상을 BLUE로 변경
            when (type)
            {
                InfoConst.EMAIL -> emailStatusType.value = CustomEditText.STATUS_COLOR_BLUE
                InfoConst.PASSWORD -> pwdStatusType.value = CustomEditText.STATUS_COLOR_BLUE
            }
        }
        else
        {
            when (type)
            {
                InfoConst.EMAIL ->
                {
                    // email text가 공백일 때
                    if (TextUtils.isEmpty(email.value))
                    {
                        emailStatusType.value = CustomEditText.STATUS_COLOR_RED
                        emailValidateText.value = "이메일을 입력해주세요."

                        setVisibleState(
                            type = type,
                            errorState = View.VISIBLE,
                            corState = View.GONE
                        )

                        /**
                         *  email, pwd 전체 유효성 검사
                         */
                        _result.value = onCheckValidate()
                        return@FocusChangeCallback
                    }

                    // email 단독 유효성 검사
                    val isValidate = ValidateUtil.isValidateEmail(email = email.value)

                    // email 유효성 통과
                    if (isValidate)
                    {
                        emailStatusType.value = CustomEditText.STATUS_COLOR_BLUE
                        setVisibleState(
                            type = InfoConst.EMAIL,
                            errorState = View.GONE,
                            corState = View.VISIBLE
                        )
                    }
                    // email 유효성 에러
                    else
                    {
                        emailStatusType.value = CustomEditText.STATUS_COLOR_RED
                        emailValidateText.value = "이메일 형식을 확인해주세요."
                        setVisibleState(
                            type = type,
                            errorState = View.VISIBLE,
                            corState = View.GONE
                        )
                    }
                    _result.value = onCheckValidate()
                }
                InfoConst.PASSWORD ->
                {
                    // password가 공백일 때
                    if (TextUtils.isEmpty(pwd.value))
                    {
                        pwdStatusType.value = CustomEditText.STATUS_COLOR_RED
                        pwdValidateText.value = "비밀번호를 입력해주세요."
                        setVisibleState(type, View.VISIBLE, View.GONE)
                        _result.value = onCheckValidate()

                        return@FocusChangeCallback
                    }

                    // password 유효성 검사
                    val isValidate = ValidateUtil.isValidatePassword(pwd = pwd.value)

                    // password 유효성 검사 통과
                    if (isValidate)
                    {
                        pwdStatusType.value = CustomEditText.STATUS_COLOR_BLUE
                        setVisibleState(type, View.GONE, View.VISIBLE)
                    }
                    // password 유효성 에러
                    else
                    {
                        pwdStatusType.value = CustomEditText.STATUS_COLOR_RED
                        pwdValidateText.value = "비밀번호 형식을 확인해주세요."
                        setVisibleState(type, View.VISIBLE, View.GONE)
                    }
                    _result.value = onCheckValidate()
                }
            }
        }
    }

    // email, password 입력 상태 확인
    private fun onCheckValidate(): Result<*, *>
    {
        SopoLog.d(msg = "onCheckValidate call()")
        // email 유효성 에러
        if (isEmailCorVisible.value != View.VISIBLE)
        {
            SopoLog.d( msg = "Validate Fail Email ")
            emailStatusType.value = CustomEditText.STATUS_COLOR_RED
            return Result<Unit, Unit>(
                errorResult = ErrorResult(
                    code = null, errorMsg = emailValidateText.value.toString(), errorType = ErrorResult.ERROR_TYPE_NON, data = null
                )
            )
        }

        // password 유효성 에러
        if (isPwdCorVisible.value != View.VISIBLE)
        {
            SopoLog.d( msg = "Validate Fail PWD ")
            pwdStatusType.value = CustomEditText.STATUS_COLOR_RED
            return Result<Unit, Unit>(
                errorResult = ErrorResult(
                    code = null, errorMsg = pwdValidateText.value.toString(), errorType = ErrorResult.ERROR_TYPE_NON, data = null
                )
            )
        }

        return Result<Unit?, Unit?>(
            successResult = SuccessResult(
                code = null, successMsg = "SUCCESS", data = null
            )
        )
    }

    fun onLoginClicked(v: View)
    {
        SopoLog.d( msg = "onLoginClicked() call!!!")

        _result.value = onCheckValidate()

        // result가 전부 통과일 때
        when
        {
            _result.value?.successResult != null ->
            {
                setProgressValue(true)

                // Firebase email login
                FirebaseRepository.firebaseSelfLogin(
                    email = email.value!!, password = pwd.value!!
                ) { s1, e1 ->
                    if (e1 != null)
                    {
                        setProgressValue(false)
                        postResultValue(successResult = s1, errorResult = e1)

                        return@firebaseSelfLogin
                    }

                    if (s1 != null)
                    {
                        val user = s1.data

                        SopoLog.d( msg = "Auth Email")

                        uid = user?.uid!!

                        LoginHandler.oAuthLogin(email = email.value.toString(), password = pwd.value.toString(), deviceInfo = OtherUtil.getDeviceID(SOPOApp.INSTANCE)){ s2, e2 ->

                            if(e2 != null)
                            {
                                setProgressValue(false)
                                postResultValue(successResult = s2, errorResult = e2)
                            }

                            if(s2 != null)
                            {
                                setProgressValue(false)
                                postResultValue(successResult = s2, errorResult = e2)
                            }
                        }
                    }

                }
            }
            // result가 에러일 때
            _result.value?.errorResult != null ->
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
            else ->
            {
                SopoLog.d(msg = "Total Result All NULL")
            }
        }
    }
}