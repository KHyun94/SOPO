package com.delivery.sopo.viewmodels

import android.text.TextUtils
import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.consts.JoinTypeConst
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.extentions.commonMessageResId
import com.delivery.sopo.firebase.FirebaseUserManagement
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.LoginResult
import com.delivery.sopo.models.ValidateResult
import com.delivery.sopo.networks.api.LoginAPI
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.UserAPI
import com.delivery.sopo.repository.shared.UserRepo
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.ValidateUtil
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginViewModel(val userRepo: UserRepo) : ViewModel()
{

    val TAG = "LOG.SOPO.LoginVM"

    var email = MutableLiveData<String>()
    var pwd = MutableLiveData<String>()
    var uid = ""

    var emailValidateText = MutableLiveData<String>()
    var pwdValidateText = MutableLiveData<String>()

    var isEmailErrorVisible = MutableLiveData<Int>()
    var isPwdErrorVisible = MutableLiveData<Int>()

    var isEmailCorVisible = MutableLiveData<Int>()
    var isPwdCorVisible = MutableLiveData<Int>()

    var emailStatusType = MutableLiveData<Int>()
    var pwdStatusType = MutableLiveData<Int>()

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

        emailStatusType.value = -1
        pwdStatusType.value = -1
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
                        emailStatusType.value = 0
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
                        emailStatusType.value = 1
                        setVisibleState(
                            type = InfoConst.EMAIL,
                            errorState = View.GONE,
                            corState = View.VISIBLE
                        )
                    }
                    else
                    {
                        emailStatusType.value = 0
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
                        pwdStatusType.value = 0
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
                        pwdStatusType.value = 1
                        setVisibleState(type, View.GONE, View.VISIBLE)
                    }
                    else
                    {
                        pwdStatusType.value = 0
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
            emailStatusType.value = 0
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
            pwdStatusType.value = 0
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

                            uid = it.result?.user?.uid!!

                            requestLogin(
                                email = email.value.toString(),
                                pwd = pwd.value.toString(),
                                deviceInfo = OtherUtil.getDeviceID(SOPOApp.INSTANCE),
                                joinType = "self",
                                uid = uid
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

    fun authJwtToken(jwtToken: String)
    {
        NetworkManager.publicRetro.create(UserAPI::class.java)
            .requestUpdateDeviceInfo(
                email = email.value.toString(),
                jwtToken = jwtToken
            ).enqueue(object : Callback<APIResult<String?>>
            {
                override fun onFailure(call: Call<APIResult<String?>>, t: Throwable)
                {
                    TODO("Not yet implemented")
                }

                override fun onResponse(
                    call: Call<APIResult<String?>>,
                    response: Response<APIResult<String?>>
                )
                {
                    val httpStatusCode = response.code()

                    val result = response.body()

                    when (httpStatusCode)
                    {
                        200 ->
                        {
                            if (result?.code == ResponseCode.SUCCESS.CODE)
                            {
                                requestLogin(
                                    email = email.value.toString(),
                                    pwd = pwd.value.toString(),
                                    deviceInfo = OtherUtil.getDeviceID(SOPOApp.INSTANCE),
                                    joinType = JoinTypeConst.SELF,
                                    uid = uid
                                )
                            }
                            else
                            {
                                validateResult.value =
                                    ValidateResult(
                                        false,
                                        CodeUtil.returnCodeMsg(result?.code),
                                        jwtToken,
                                        InfoConst.CUSTOM_DIALOG
                                    )
                            }
                        }
                        else ->
                        {
                            validateResult.value =
                                ValidateResult(
                                    false,
                                    CodeUtil.returnCodeMsg(result?.code),
                                    jwtToken,
                                    InfoConst.CUSTOM_DIALOG
                                )
                        }
                    }

                }

            })
    }

    private fun requestLogin(
        email: String,
        pwd: String,
        deviceInfo: String,
        joinType: String,
        uid: String
    )
    {
        NetworkManager.publicRetro.create(LoginAPI::class.java)
            .requestSelfLogin(
                email = email,
                pwd = pwd,
                deviceInfo = deviceInfo,
                joinType = joinType,
                uid = uid
            ).enqueue(object : Callback<APIResult<Any?>>
            {
                override fun onFailure(call: Call<APIResult<Any?>>, t: Throwable)
                {
                    validateResult.value =
                        ValidateResult(false, t.message!!, 1, InfoConst.CUSTOM_DIALOG)
                }

                override fun onResponse(
                    call: Call<APIResult<Any?>>,
                    response: Response<APIResult<Any?>>
                )
                {
                    val httpStatusCode = response.code()

                    val result = response.body()

                    when (httpStatusCode)
                    {
                        200 ->
                        {
                            when (result?.code)
                            {
                                ResponseCode.SUCCESS.CODE ->
                                {
                                    Log.d(TAG, "What the fuck ${result.data.toString()}")
                                    val gson = Gson()

                                    val type = object : TypeToken<LoginResult?>()
                                    {}.type

                                    val reader = gson.toJson(result.data)

                                    val user = gson.fromJson<LoginResult>(reader, type)

                                    userRepo.setEmail(email = user.userName)
                                    userRepo.setApiPwd(pwd = user.password)
                                    userRepo.setDeviceInfo(info = deviceInfo)
                                    userRepo.setJoinType(joinType = joinType)
                                    userRepo.setRegisterDate(user.regDt)
                                    userRepo.setStatus(user.status)

                                    validateResult.value =
                                        ValidateResult(
                                            true,
                                            result.message,
                                            result,
                                            InfoConst.NON_SHOW
                                        )
                                }
                                ResponseCode.ALREADY_LOGGED_IN.CODE ->
                                {
                                    val jwtToken = result.data as String

                                    validateResult.value =
                                        ValidateResult(
                                            false,
                                            CodeUtil.returnCodeMsg(result.code),
                                            jwtToken,
                                            InfoConst.CUSTOM_DIALOG
                                        )
                                }
                                else ->
                                {
                                    validateResult.value =
                                        ValidateResult(
                                            false,
                                            CodeUtil.returnCodeMsg(result?.code),
                                            null,
                                            InfoConst.CUSTOM_DIALOG
                                        )
                                }
                            }
                        }
                        else ->
                        {
                            validateResult.value =
                                ValidateResult(
                                    false,
                                    CodeUtil.returnCodeMsg(result?.code!!),
                                    null,
                                    InfoConst.CUSTOM_DIALOG
                                )
                        }
                    }
                }
            })
    }
}