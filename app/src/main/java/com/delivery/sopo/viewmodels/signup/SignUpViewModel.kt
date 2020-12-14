package com.delivery.sopo.viewmodels.signup

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.extensions.commonMessageResId
import com.delivery.sopo.firebase.FirebaseUserManagement
import com.delivery.sopo.models.ValidateResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.UserAPI
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.google.firebase.analytics.FirebaseAnalytics
import com.kakao.usermgmt.api.UserApi
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers.io

typealias FocusChangeCallback = (String, Boolean) -> Unit

class SignUpViewModel : ViewModel()
{
    val TAG = "LOG.SOPO" + this.javaClass.simpleName

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

    var validateResult = MutableLiveData<ValidateResult<Any?>>()
    var isAgree = MutableLiveData<Boolean>()

    var isDuplicate = true

    init
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

        emailStatusType.value = -1
        pwdStatusType.value = -1
        rePwdStatusType.value = -1

        validateResult.value = ValidateResult(false, "회원정보를 입력해주세요!!", null, InfoConst.NON_SHOW)

        isAgree.value = false
    }

    fun onAgreeClicked()
    {
        val currentStatus = isAgree.value
        isAgree.value = !currentStatus!!
    }

    fun onSignUpClicked(v: View)
    {
        if (!v.hasFocus())
            v.requestFocus()
        else
            validateResult.value = onCheckValidate()

        if (validateResult.value?.result == true)
        {
            signUpWithFirebase(this.email.value!!, this.pwd.value!!)
        }
        else
        {

            val type = if (validateResult.value?.showType == InfoConst.CUSTOM_DIALOG)
            {
                InfoConst.CUSTOM_DIALOG
            }
            else
            {
                InfoConst.CUSTOM_TOAST_MSG
            }

            validateResult.value?.showType = type
            val tmp = validateResult.value
            validateResult.value = tmp
        }

        SopoLog.d( tag = TAG, str = "SignUp Click!!!!!!!!!!!!!")
    }

    // EditText 유효성 검사 가시성
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
            InfoConst.RE_PASSWORD ->
            {
                isRePwdErrorVisible.value = errorState
                isRePwdCorVisible.value = corState
            }
        }
    }

    // Custom EditText 포커스 변화 콜백
    var callback: FocusChangeCallback = FocusChangeCallback@{ type, focus ->

        if (focus)
        {
            SopoLog.d( tag = TAG, str = "Focus In $type")
            setVisibleState(type = type, errorState = GONE, corState = GONE)

            if (type == InfoConst.EMAIL)
                isDuplicate = true
        }
        else
        {
            SopoLog.d( tag = TAG, str = "Focus Out $type")

            when (type)
            {
                InfoConst.EMAIL ->
                {
                    // 이메일 란이 공백일 때
                    if (TextUtils.isEmpty(email.value))
                    {
                        emailStatusType.value = 0
                        emailValidateText.value = "이메일을 입력해주세요."
                        setVisibleState(type = type, errorState = VISIBLE, corState = GONE)
                        validateResult.value = onCheckValidate()
                        SopoLog.d( tag = TAG, str = "Email is Empty")

                        return@FocusChangeCallback
                    }

                    // 이메일 유효성 검사
                    val isValidate = ValidateUtil.isValidateEmail(email = email.value)

                    if (isValidate)
                    {
                        // 이메일이 중복 이메일이거나 초기화되었을 때 중복 api로 통신
                        if (isDuplicate)
                        {
                            SopoLog.d( tag = TAG, str = "Duplicate Check Start!!!")
                            onCheckDuplicatedEmail(email = email.value!!)

                            return@FocusChangeCallback
                        }
                        else
                        {
                            emailStatusType.value = 1
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
                        SopoLog.d( tag = TAG, str = "PLZ Check Email Validate")
                        emailValidateText.value = "이메일 형식을 확인해주세요."
                        setVisibleState(type = type, errorState = VISIBLE, corState = GONE)
                    }

                    // 이메일, 비밀번호, 비밀번호 확인, 이용약관 유효성 검사
                    validateResult.value = onCheckValidate()
                }
                InfoConst.PASSWORD ->
                {
                    SopoLog.d( tag = TAG, str = "Focus out $type ${pwd.value}")

                    // 비밀번호 란이 공백일 때
                    if (TextUtils.isEmpty(pwd.value))
                    {
                        pwdStatusType.value = 0

                        SopoLog.d( tag = TAG, str = "pwd is empty")

                        pwdValidateText.value = "비밀번호를 입력해주세요."
                        setVisibleState(type = type, errorState = VISIBLE, corState = GONE)
                        validateResult.value = onCheckValidate()

                        return@FocusChangeCallback
                    }

                    // 비밀번호 유효성 검사
                    val isValidate = ValidateUtil.isValidatePassword(pwd = pwd.value)

                    if (isValidate)
                    {
                        pwdStatusType.value = 1
                        setVisibleState(type, GONE, VISIBLE)

                        // 유효성 통과 시 비밀번호 확인 체크
                        if (pwd.value == rePwd.value)
                        {
                            pwdStatusType.value = 1
                            rePwdStatusType.value = 1
                            setVisibleState(InfoConst.RE_PASSWORD, GONE, VISIBLE)
                        }
                    }
                    else
                    {
                        pwdStatusType.value = 0
                        pwdValidateText.value = "비밀번호 형식을 확인해주세요."
                        setVisibleState(type, VISIBLE, GONE)

                        // 비밀번호 일치 검사
                        if (rePwd.value!!.isNotEmpty())
                        {
                            rePwdStatusType.value = 0
                            rePwdValidateText.value = "비밀번호가 일치하지 않습니다."
                            setVisibleState(InfoConst.RE_PASSWORD, VISIBLE, GONE)
                        }
                    }

                    validateResult.value = onCheckValidate()
                }
                InfoConst.RE_PASSWORD ->
                {
                    SopoLog.d( tag = TAG, str = "type $type re_pwd ${rePwd.value}")

                    // 비밀번호가 최소 1자리 이상일 때
                    if (pwd.value!!.isNotEmpty())
                    {
                        // 비밀번호 확인 란이 공백일 때
                        if (TextUtils.isEmpty(rePwd.value))
                        {
                            rePwdStatusType.value = 0
                            rePwdValidateText.value = "비밀번호 확인을 입력해주세요."
                            setVisibleState(type = type, errorState = VISIBLE, corState = GONE)
                            validateResult.value = onCheckValidate()

                            return@FocusChangeCallback
                        }

                        val isPwdValidate = ValidateUtil.isValidatePassword(pwd = pwd.value)
                        val isRePwdValidate = ValidateUtil.isValidatePassword(pwd = rePwd.value)

                        if (isPwdValidate && pwd.value == rePwd.value)
                        {
                            rePwdStatusType.value = 1
                            // 비밀번호 유효성이 true일 때 비밀번호가 일치하는지
                            setVisibleState(InfoConst.RE_PASSWORD, GONE, VISIBLE)
                        }
                        else if (!isRePwdValidate)
                        {
                            if (isPwdValidate)
                                pwdStatusType.value = 1
                            else
                                pwdStatusType.value = 0

                            rePwdStatusType.value = 0
                            // 비밀번호 확인의 유효성이 false 일 때
                            rePwdValidateText.value = "비밀번호 형식을 확인해주세요."
                            setVisibleState(InfoConst.RE_PASSWORD, VISIBLE, GONE)
                        }
                        else
                        {
                            pwdStatusType.value = 0
                            rePwdStatusType.value = 0
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

                    validateResult.value = onCheckValidate()
                }
            }
        }

    }

    private fun onCheckValidate(): ValidateResult<Any?>
    {
        if (isEmailCorVisible.value != VISIBLE)
        {
            SopoLog.d( tag = TAG, str = "Validate Fail Email ")
            emailStatusType.value = 0
            return ValidateResult(
                result = false,
                msg = emailValidateText.value.toString(),
                data = null,
                showType = InfoConst.NON_SHOW
            )
        }

        if (isPwdCorVisible.value != VISIBLE)
        {

            pwdStatusType.value = 0
            SopoLog.d( tag = TAG, str = "Validate Fail PWD ")

            return ValidateResult(
                result = false,
                msg = pwdValidateText.value.toString(),
                data = null,
                showType = InfoConst.NON_SHOW
            )
        }

        if (isRePwdCorVisible.value != VISIBLE)
        {

            rePwdStatusType.value = 0
            SopoLog.d( tag = TAG, str = "Validate Fail PWD ")

            return ValidateResult(
                result = false,
                msg = rePwdValidateText.value.toString(),
                data = null,
                showType = InfoConst.NON_SHOW
            )
        }

        if (!isAgree.value!!)
        {

            SopoLog.d( tag = TAG, str = "Validate Fail Agree ")


            return ValidateResult(
                result = false,
                msg = "사용자 약관 이용에 동의해주세요!!!",
                data = null,
                showType = InfoConst.NON_SHOW
            )
        }


        SopoLog.d( tag = TAG, str = "Validate Success ")


        return ValidateResult(result = true, msg = "", data = null, showType = InfoConst.NON_SHOW)
    }

    private fun onCheckDuplicatedEmail(email: String)
    {
        NetworkManager.run {
            publicRetro.create(UserAPI::class.java).requestDuplicateEmail(email)
                .subscribeOn(io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {

                        // todo 성공 코드('0000')이 아닐 때 중복 이메일이라고 표시(임시)
                        isDuplicate = if (it.code == "0000") it.data!! else false

                        SopoLog.d( tag = TAG, str = "Duplicate Result => $isDuplicate")

                        // isDuplicate가 false일 때 사용 가능 이메일
                        if (!isDuplicate)
                        {
                            emailStatusType.value = 1
                            setVisibleState(
                                type = InfoConst.EMAIL,
                                errorState = GONE,
                                corState = VISIBLE
                            )
                            SopoLog.d( tag = TAG, str = "it is possible to use Email!!!")
                        }
                        else
                        {
                            emailStatusType.value = 0
                            isDuplicate = true
                            emailValidateText.value = "중복된 이메일입니다."
                            setVisibleState(
                                type = InfoConst.EMAIL,
                                errorState = VISIBLE,
                                corState = GONE
                            )
                            SopoLog.d( tag = TAG, str = "it is Duplicate Email!!!")
                        }

                        validateResult.value = onCheckValidate()
                    },
                    {
                        isDuplicate = true

                        emailValidateText.value = "알수 없는 오류"
                        setVisibleState(
                            type = InfoConst.EMAIL,
                            errorState = VISIBLE,
                            corState = GONE
                        )

                        validateResult.value = onCheckValidate()
                    }
                )
        }

    }

    fun signUpWithFirebase(email: String, pwd: String)
    {
        SopoLog.d( tag = TAG, str = "Firebase Sign Up Start~!!!")

        FirebaseUserManagement.firebaseCreateUser(email, pwd)
            .addOnCompleteListener {
                when
                {
                    it.isSuccessful ->
                    {
                        val user = SOPOApp.auth.currentUser

                        SopoLog.d( tag = TAG, str = "Firebase Sign Up User - ${user?.email ?: "이메일 없음"}")

                        FirebaseUserManagement.firebaseSendEmail(user!!)
                            ?.addOnCompleteListener {
                                SopoLog.d( tag = TAG, str = "Firebase Send Auth Email")

                                val bundle = Bundle()
                                bundle.putString(FirebaseAnalytics.Param.METHOD, "email")
                                FirebaseAnalytics.getInstance(SOPOApp.INSTANCE)
                                    .logEvent(FirebaseAnalytics.Event.SIGN_UP, bundle)

                                if (it.isSuccessful)
                                {
                                    validateResult.value = ValidateResult(
                                        result = true,
                                        msg = "",
                                        data = "Success",
                                        showType = InfoConst.CUSTOM_DIALOG
                                    )
                                }
                                else
                                {
                                    validateResult.value = ValidateResult(
                                        result = false,
                                        msg = "이메일 인증 메일 전송에 실패했습니다. 다시 한번 시도해주세요.",
                                        data = null,
                                        showType = InfoConst.CUSTOM_DIALOG
                                    )
                                }

                                SopoLog.d( tag = TAG, str = validateResult.value.toString())
                            }
                    }
                    else ->
                    {
                        SopoLog.d( tag = TAG, str = "에러${it.exception?.commonMessageResId}")
                        // 회원가입 실패
                        validateResult.value = ValidateResult(
                            result = false,
                            msg = it.exception?.localizedMessage!!,
                            data = it.exception?.commonMessageResId,
                            showType = InfoConst.CUSTOM_DIALOG
                        )
                    }
                }
            }
    }
}