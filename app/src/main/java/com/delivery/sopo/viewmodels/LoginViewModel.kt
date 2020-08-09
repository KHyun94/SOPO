package com.delivery.sopo.viewmodels

import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.extentions.commonMessageResId
import com.delivery.sopo.firebase.FirebaseUserManagement
import com.delivery.sopo.models.ValidateResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.UserRepo
import com.delivery.sopo.util.fun_util.OtherUtil
import com.delivery.sopo.util.fun_util.ValidateUtil
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers.io

class LoginViewModel(val userRepo: UserRepo) : ViewModel()
{

    val TAG = "LOG.SOPO.LoginVM"

    var email = MutableLiveData<String>()
    var pwd = MutableLiveData<String>()

    var emailValidateText = MutableLiveData<String>()
    var pwdValidateText = MutableLiveData<String>()

    var isEmailErrorVisible = MutableLiveData<Int>()
    var isPwdErrorVisible = MutableLiveData<Int>()

    var isEmailCorVisible = MutableLiveData<Int>()
    var isPwdCorVisible = MutableLiveData<Int>()

    var validateResult = MutableLiveData<ValidateResult<Any?>>()

    init
    {
        email.value = ""
        pwd.value = ""
        isEmailErrorVisible.value = View.GONE
        isPwdErrorVisible.value = View.GONE

        isEmailCorVisible.value = View.GONE
        isPwdCorVisible.value = View.GONE

        emailValidateText.value = "이메일을 입력해주세요."
        pwdValidateText.value = "비밀번호를 입력해주세요."
    }

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

    var callback: FocusChangeCallback = FocusChangeCallback@{ type, focus ->
        if (focus)
        {
            setVisibleState(type = type, errorState = View.GONE, corState = View.GONE)
        }
        else
        {
            when (type)
            {
                InfoConst.EMAIL ->
                {
                    if (TextUtils.isEmpty(email.value))
                    {
                        emailValidateText.value = "이메일을 입력해주세요."
                        setVisibleState(
                            type = type,
                            errorState = View.VISIBLE,
                            corState = View.GONE
                        )
                        validateResult.value = onCheckValidate()
                        return@FocusChangeCallback
                    }

                    val isValidate = ValidateUtil.isValidateEmail(email = email.value)

                    if (isValidate)
                    {
                        setVisibleState(
                            type = InfoConst.EMAIL,
                            errorState = View.GONE,
                            corState = View.VISIBLE
                        )
                    }
                    else
                    {
                        emailValidateText.value = "이메일 형식을 확인해주세요."
                        setVisibleState(
                            type = type,
                            errorState = View.VISIBLE,
                            corState = View.GONE
                        )
                    }
                    validateResult.value = onCheckValidate()
                }
                InfoConst.PASSWORD ->
                {
                    if (TextUtils.isEmpty(pwd.value))
                    {
                        pwdValidateText.value = "비밀번호를 입력해주세요."
                        setVisibleState(
                            type = type,
                            errorState = View.VISIBLE,
                            corState = View.GONE
                        )

                        validateResult.value = onCheckValidate()

                        return@FocusChangeCallback
                    }

                    val isValidate = ValidateUtil.isValidatePassword(pwd = pwd.value)

                    if (isValidate)
                    {
                        setVisibleState(type, View.GONE, View.VISIBLE)
                    }
                    else
                    {
                        pwdValidateText.value = "비밀번호 형식을 확인해주세요."
                        setVisibleState(type, View.VISIBLE, View.GONE)
                    }

                    validateResult.value = onCheckValidate()
                }
            }
        }
    }

    private fun onCheckValidate(): ValidateResult<Any?>
    {
        if (isEmailCorVisible.value != View.VISIBLE)
        {
            Log.d(TAG, "Validate Fail Email ")

            return ValidateResult(
                result = false,
                msg = emailValidateText.value.toString(),
                data = null,
                showType = InfoConst.NON_SHOW
            )
        }

        if (isPwdCorVisible.value != View.VISIBLE)
        {

            Log.d(TAG, "Validate Fail PWD ")

            return ValidateResult(
                result = false,
                msg = pwdValidateText.value.toString(),
                data = null,
                showType = InfoConst.NON_SHOW
            )
        }

        return ValidateResult(result = true, msg = "", data = null, showType = InfoConst.NON_SHOW)
    }

    fun onLoginClicked(v: View)
    {
        if (!v.hasFocus())
            v.requestFocus()
        else
            validateResult.value = onCheckValidate()

        if (validateResult.value?.result == true)
        {
            FirebaseUserManagement.firebaseGeneralLogin(email = email.value!!, pwd = pwd.value!!)
                .addOnCompleteListener {
                    if (it.isSuccessful)
                    {
                        Log.d(TAG, "Firebase Login")
                        if (SOPOApp.auth.currentUser?.isEmailVerified!!)
                        {
                            Log.d(TAG, "Auth Email")
                            requestLogin(
                                email.value.toString(),
                                pwd.value.toString(),
                                OtherUtil.getDeviceID(SOPOApp.INSTANCE),
                                "self"
                            )
                        }
                        else
                        {
                            Log.d(TAG, "Not Auth Email")
                            validateResult.value =
                                ValidateResult(false, "이메일 인증을 해주세요.", 1, InfoConst.CUSTOM_DIALOG)
                        }
                    }
                    else
                    {
                        Log.d(TAG, "Firebase Login Fail ${it.exception?.message}")
                        validateResult.value = ValidateResult(
                            false,
                            "이메일 인증을 해주세요.",
                            it.exception?.commonMessageResId,
                            InfoConst.CUSTOM_DIALOG
                        )
                    }
                }

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
    }

    private fun requestLogin(email: String, pwd: String, deviceInfo: String, joinType: String)
    {
        Log.d(TAG, "info $deviceInfo")

        NetworkManager.getUserAPI().run {
            requestSelfLogin(
                email = email,
                pwd = pwd,
                deviceInfo = deviceInfo,
                joinType = joinType
            )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(io())
                .subscribe(
                    {
                        Log.d(TAG, "성공: $it")

                        if (it.code == ResponseCode.SUCCESS.CODE)
                        {
                            validateResult.value =
                                ValidateResult(true, it.message, it, InfoConst.NON_SHOW)

                            userRepo.setEmail(email = it.data?.userName!!)
                            userRepo.setApiPwd(pwd = it.data?.password!!)
                            userRepo.setDeviceInfo(info = deviceInfo)
                            userRepo.setJoinType(joinType = joinType)
                            userRepo.setRegisterDate(it.data?.regDt!!)
                            userRepo.setStatus(it.data?.status!!)
                        }
                        else
                        {
                            validateResult.value =
                                ValidateResult(false, it.message, it, InfoConst.CUSTOM_DIALOG)
                        }

                    },
                    {
                        Log.d(TAG, "실패: $it")
                        validateResult.value = ValidateResult(false, it.toString(), it, InfoConst.CUSTOM_DIALOG)
                    }
                )
        }
    }
}