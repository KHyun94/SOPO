package com.delivery.sopo.viewmodels

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
import com.delivery.sopo.extentions.commonMessageResId
import com.delivery.sopo.firebase.FirebaseUserManagement
import com.delivery.sopo.models.ValidateResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.util.fun_util.ValidateUtil
import com.google.firebase.analytics.FirebaseAnalytics
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

        validateResult.value = ValidateResult(false, "회원정보를 입력해주세요!!", null, InfoConst.NON_SHOW)

        isAgree.value = true
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
            Log.d(TAG, "Focus In $type")
            setVisibleState(type = type, errorState = GONE, corState = GONE)

            if (type == InfoConst.EMAIL)
                isDuplicate = true
        }
        else
        {
            Log.d(TAG, "Focus Out $type")

            when (type)
            {
                InfoConst.EMAIL ->
                {
                    // 이메일 란이 공백일 때
                    if (TextUtils.isEmpty(email.value))
                    {
                        emailValidateText.value = "이메일을 입력해주세요."
                        setVisibleState(type = type, errorState = VISIBLE, corState = GONE)
                        validateResult.value = onCheckValidate()
                        Log.d(TAG, "Email is Empty")

                        return@FocusChangeCallback
                    }

                    // 이메일 유효성 검사
                    val isValidate = ValidateUtil.isValidateEmail(email = email.value)

                    if (isValidate)
                    {
                        // 이메일이 중복 이메일이거나 초기화되었을 때 중복 api로 통신
                        if (isDuplicate)
                        {
                            Log.d(TAG, "Duplicate Check Start!!!")
                            onCheckDuplicateEmail(email = email.value!!)

                            return@FocusChangeCallback
                        }
                        else
                        {
                            setVisibleState(
                                type = InfoConst.EMAIL,
                                errorState = GONE,
                                corState = VISIBLE
                            )
                        }
                    }
                    else
                    {
                        Log.d(TAG, "PLZ Check Email Validate")
                        emailValidateText.value = "이메일 형식을 확인해주세요."
                        setVisibleState(type = type, errorState = VISIBLE, corState = GONE)
                    }

                    // 이메일, 비밀번호, 비밀번호 확인, 이용약관 유효성 검사
                    validateResult.value = onCheckValidate()
                }
                InfoConst.PASSWORD ->
                {
                    Log.d(TAG, "Focus out $type ${pwd.value}")

                    // 비밀번호 란이 공백일 때
                    if (TextUtils.isEmpty(pwd.value))
                    {

                        Log.d(TAG, "pwd is empty")

                        pwdValidateText.value = "비밀번호를 입력해주세요."
                        setVisibleState(type = type, errorState = VISIBLE, corState = GONE)
                        validateResult.value = onCheckValidate()

                        return@FocusChangeCallback
                    }

                    // 비밀번호 유효성 검사
                    val isValidate = ValidateUtil.isValidatePassword(pwd = pwd.value)

                    if (isValidate)
                    {
                        setVisibleState(type, GONE, VISIBLE)

                        // 유효성 통과 시 비밀번호 확인 체크
                        if (pwd.value == rePwd.value)
                            setVisibleState(InfoConst.RE_PASSWORD, GONE, VISIBLE)
                    }
                    else
                    {
                        pwdValidateText.value = "비밀번호 형식을 확인해주세요."
                        setVisibleState(type, VISIBLE, GONE)

                        // 비밀번호 일치 검사
                        if (rePwd.value!!.isNotEmpty())
                        {
                            rePwdValidateText.value = "비밀번호가 일치하지 않습니다."
                            setVisibleState(InfoConst.RE_PASSWORD, VISIBLE, GONE)
                        }
                    }

                    validateResult.value = onCheckValidate()
                }
                InfoConst.RE_PASSWORD ->
                {
                    Log.d(TAG, "type $type re_pwd ${rePwd.value}")

                    // 비밀번호가 최소 1자리 이상일 때
                    if (pwd.value!!.isNotEmpty())
                    {

                        // 비밀번호 확인 란이 공백일 때
                        if (TextUtils.isEmpty(rePwd.value))
                        {
                            rePwdValidateText.value = "비밀번호 확인을 입력해주세요."
                            setVisibleState(type = type, errorState = VISIBLE, corState = GONE)
                            validateResult.value = onCheckValidate()

                            return@FocusChangeCallback
                        }

                        val isPwdValidate = ValidateUtil.isValidatePassword(pwd = pwd.value)
                        val isRePwdValidate = ValidateUtil.isValidatePassword(pwd = rePwd.value)

                        if (isPwdValidate && pwd.value == rePwd.value)
                        {
                            // 비밀번호 유효성이 true일 때 비밀번호가 일치하는지
                            setVisibleState(InfoConst.RE_PASSWORD, GONE, VISIBLE)
                        }
                        else if (!isRePwdValidate)
                        {
                            // 비밀번호 확인의 유효성이 false 일 때
                            rePwdValidateText.value = "비밀번호 형식을 확인해주세요."
                            setVisibleState(InfoConst.RE_PASSWORD, VISIBLE, GONE)
                        }
                        else
                        {
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
                            // 비밀번호 확인의 유효성이 false일 때
                            rePwdValidateText.value = "비밀번호 확인의 형식을 확인해주세요."
                            setVisibleState(InfoConst.RE_PASSWORD, VISIBLE, GONE)
                        }
                        else if (isRePwdValidate)
                        {
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
            Log.d(TAG, "Validate Fail Email ")

            return ValidateResult(
                result = false,
                msg = emailValidateText.value.toString(),
                data = null,
                showType = InfoConst.NON_SHOW
            )
        }

        if (isPwdCorVisible.value != VISIBLE)
        {

            Log.d(TAG, "Validate Fail PWD ")

            return ValidateResult(
                result = false,
                msg = pwdValidateText.value.toString(),
                data = null,
                showType = InfoConst.NON_SHOW
            )
        }

        if (isRePwdCorVisible.value != VISIBLE)
        {

            Log.d(TAG, "Validate Fail PWD ")

            return ValidateResult(
                result = false,
                msg = rePwdValidateText.value.toString(),
                data = null,
                showType = InfoConst.NON_SHOW
            )
        }

        if (!isAgree.value!!)
        {

            Log.d(TAG, "Validate Fail Agree ")


            return ValidateResult(
                result = false,
                msg = "사용자 약관 이용에 동의해주세요!!!",
                data = null,
                showType = InfoConst.NON_SHOW
            )
        }


        Log.d(TAG, "Validate Success ")


        return ValidateResult(result = true, msg = "", data = null, showType = InfoConst.NON_SHOW)
    }

    private fun onCheckDuplicateEmail(email: String)
    {
        NetworkManager.run {
            getUserAPI().requestDuplicateEmail(email)
                .subscribeOn(io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {

                        // todo 성공 코드('0000')이 아닐 때 중복 이메일이라고 표시(임시)
                        isDuplicate = if (it.code == "0000") it.data!! else false

                        Log.d(TAG, "Duplicate Result => $isDuplicate")

                        // isDuplicate가 false일 때 사용 가능 이메일
                        if (!isDuplicate)
                        {
                            setVisibleState(
                                type = InfoConst.EMAIL,
                                errorState = GONE,
                                corState = VISIBLE
                            )
                            Log.d(TAG, "it is possible to use Email!!!")
                        }
                        else
                        {
                            isDuplicate = true
                            emailValidateText.value = "중복된 이메일입니다."
                            setVisibleState(
                                type = InfoConst.EMAIL,
                                errorState = VISIBLE,
                                corState = GONE
                            )
                            Log.d(TAG, "it is Duplicate Email!!!")
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

    fun onSignUpClicked(v: View)
    {
        if(!v.hasFocus())
            v.requestFocus()

        if(validateResult.value?.result == true){
            signUpWithFirebase(this.email.value!!, this.pwd.value!!)
        } else {

            val type = if(validateResult.value?.showType == InfoConst.CUSTOM_DIALOG){
                InfoConst.CUSTOM_DIALOG
            } else {
                InfoConst.CUSTOM_TOAST_MSG
            }

            validateResult.value?.showType = type
            val tmp = validateResult.value
            validateResult.value = tmp
        }

        Log.d(TAG, "SignUp Click!!!!!!!!!!!!!")
    }

    fun signUpWithFirebase(email: String, pwd: String)
    {
        Log.d(TAG, "Firebase Sign Up Start~!!!")

        FirebaseUserManagement.firebaseCreateUser(email, pwd)
            .addOnCompleteListener {
                when
                {
                    it.isSuccessful ->
                    {
                        val user = SOPOApp.auth.currentUser

                        Log.d(TAG, "Firebase Sign Up User - ${user?.email ?: "이메일 없음"}")

                        FirebaseUserManagement.firebaseSendEmail(user!!)
                            ?.addOnCompleteListener {
                                Log.d(TAG, "Firebase Send Auth Email")

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

                                Log.d(TAG, validateResult.value.toString())
                            }
                    }
                    else ->
                    {
                        Log.d(TAG, "에러${it.exception?.commonMessageResId}")
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