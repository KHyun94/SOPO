package com.delivery.sopo.viewmodels.login

import android.text.TextUtils
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.enums.ResponseCodeEnum
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.extensions.commonMessageResId
import com.delivery.sopo.firebase.FirebaseManagementImpl
import com.delivery.sopo.mapper.OauthMapper
import com.delivery.sopo.models.OauthResult
import com.delivery.sopo.models.SopoJsonPatch
import com.delivery.sopo.models.ValidateResult
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.LoginAPICall
import com.delivery.sopo.networks.api.UserAPI
import com.delivery.sopo.networks.api.UserAPICall
import com.delivery.sopo.networks.dto.JsonPatchDto
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.Result
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.viewmodels.signup.FocusChangeCallback
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class LoginViewModel(val userRepoImpl: UserRepoImpl) : ViewModel()
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
            SopoLog.d(tag = TAG, msg = "Validate Fail Email ")
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

            SopoLog.d(tag = TAG, msg = "Validate Fail PWD ")
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
            FirebaseManagementImpl.firebaseGeneralLogin(email = email.value!!, pwd = pwd.value!!)
                .addOnCompleteListener {
                    if (it.isSuccessful)
                    {
                        SopoLog.d(tag = TAG, msg = "Firebase Login")
                        if (SOPOApp.auth.currentUser?.isEmailVerified!!)
                        {
                            SopoLog.d(tag = TAG, msg = "Auth Email")

                            uid = it.result?.user?.uid!!

                            requestLoginWithOauth(
                                email = email.value.toString(),
                                password = pwd.value.toString(),
                                deviceInfo = OtherUtil.getDeviceID(SOPOApp.INSTANCE)
                            )
                        }
                        else
                        {
                            SopoLog.d(tag = TAG, msg = "Not Auth Email ===> ${it.result.user?.uid}")
                            validateResult.value =
                                ValidateResult(false, "이메일 인증을 해주세요.", 1, InfoConst.CUSTOM_DIALOG)
                        }
                    }
                    else
                    {
                        SopoLog.d(tag = TAG, msg = "Firebase Login Fail ${it.exception?.message}")
                        validateResult.value = ValidateResult(
                            false,
                            it.exception?.message ?: "",
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
        CoroutineScope(Dispatchers.IO).launch {

            val jsonPatchList = mutableListOf<SopoJsonPatch>()
            jsonPatchList.add(
                SopoJsonPatch(
                    "replace",
                    "/deviceInfo",
                    OtherUtil.getDeviceID(SOPOApp.INSTANCE)
                )
            )

            val result = UserAPICall().patchUser(
                email = userRepoImpl.getEmail(),
                jwtToken = jwtToken,
                jsonPatch = JsonPatchDto(jsonPatchList)
            )

            when (result)
            {
                is Result.Success ->
                {
                    val apiResult = result.data

                    if (apiResult.code == ResponseCodeEnum.SUCCESS.CODE)
                    {
                        requestLoginWithOauth(
                            email = email.value.toString(),
                            password = pwd.value.toString(),
                            deviceInfo = OtherUtil.getDeviceID(SOPOApp.INSTANCE)
                        )
                    }
                    else
                    {
                        validateResult.postValue(
                            ValidateResult(
                                false,
                                CodeUtil.returnCodeMsg(apiResult.code),
                                jwtToken,
                                InfoConst.CUSTOM_DIALOG
                            )
                        )
                    }
                }
                is Result.Error ->
                {
                    val exception = result.exception as APIException
                    val apiResult = exception.data()

                    validateResult.postValue(
                        ValidateResult(
                            false,
                            CodeUtil.returnCodeMsg(apiResult?.code),
                            jwtToken,
                            InfoConst.CUSTOM_DIALOG
                        )
                    )
                }
            }
        }
    }

    private fun requestLoginWithOauth(email: String, password: String, deviceInfo: String)
    {
        CoroutineScope(Dispatchers.IO).launch {
            val result = LoginAPICall().requestOauth(email, deviceInfo, password)

            when (result)
            {
                is Result.Success ->
                {
                    SopoLog.d(msg = "requestOauth Success => ${result}")

                    val gson = Gson()

                    val type = object : TypeToken<OauthResult>()
                    {}.type

                    val reader = gson.toJson(result.data)

                    val data = gson.fromJson<OauthResult>(reader, type)

                    OauthMapper.objectToEntity(email, data)

                    if (data.expiresIn == "0")
                    {
                        requestRefreshOauth(
                            email,
                            data.refreshToken,
                            OtherUtil.getDeviceID(SOPOApp.INSTANCE)
                        )
                    }
                    else
                    {
                        userRepoImpl.setEmail(email = email)
                        userRepoImpl.setDeviceInfo(info = deviceInfo)
                        userRepoImpl.setJoinType(joinType = "self")
                        userRepoImpl.setStatus(1)

                        validateResult.postValue(
                            ValidateResult(
                                true,
                                "",
                                result,
                                InfoConst.NON_SHOW
                            )
                        )

                    }
                }
                is Result.Error ->
                {
                    SopoLog.e(msg = "requestOauth Fail => ${result}")
                    val exception = result.exception as APIException
                    val apiResult = exception.data()

                    SopoLog.d(msg = "API Result => ${apiResult} \n code = {${apiResult?.code}")

                    when (apiResult?.code)
                    {
                        ResponseCodeEnum.ALREADY_LOGGED_IN.CODE ->
                        {
                            SopoLog.d(msg = "Already_logged_in")

                            val jwtToken = apiResult.data as String

                            validateResult.postValue(
                                ValidateResult(
                                    false,
                                    CodeUtil.returnCodeMsg(apiResult.code),
                                    jwtToken,
                                    InfoConst.CUSTOM_DIALOG
                                )
                            )
                        }
                        else ->
                        {
                            validateResult.postValue(
                                ValidateResult(
                                    false,
                                    CodeUtil.returnCodeMsg(exception.data()?.code),
                                    null,
                                    InfoConst.CUSTOM_DIALOG
                                )
                            )
                        }
                    }
                }
            }

        }
    }

    private fun requestRefreshOauth(email: String, refreshToken: String, deviceInfo: String)
    {
        CoroutineScope(Dispatchers.IO).launch {
            val result = LoginAPICall().requestRefreshOauth(
                email = email,
                refreshToken = refreshToken,
                deviceInfo = deviceInfo
            )

            when (result)
            {
                is Result.Success ->
                {
                    SopoLog.d(msg = "requestOauth Success => ${result}")

                    val gson = Gson()

                    val type = object : TypeToken<OauthResult>()
                    {}.type

                    val reader = gson.toJson(result.data)

                    val data = gson.fromJson<OauthResult>(reader, type)

                    OauthMapper.objectToEntity(email, data)

                    if (data.expiresIn == "0")
                    {
                        requestRefreshOauth(
                            email,
                            data.refreshToken,
                            OtherUtil.getDeviceID(SOPOApp.INSTANCE)
                        )
                    }
                    else
                    {
                        userRepoImpl.setEmail(email = email)
                        userRepoImpl.setDeviceInfo(info = deviceInfo)
                        userRepoImpl.setJoinType(joinType = "self")
                        userRepoImpl.setStatus(1)

                        validateResult.postValue(
                            ValidateResult(
                                true,
                                "",
                                result,
                                InfoConst.NON_SHOW
                            )
                        )

                    }
                }
                is Result.Error ->
                {
                    SopoLog.e(msg = "requestOauth Fail => ${result}")
                    val exception = result.exception as APIException
                    val apiResult = exception.data()

                    SopoLog.d(msg = "API Result => ${apiResult} \n code = {${apiResult?.code}")

                    when (apiResult?.code)
                    {
                        ResponseCodeEnum.ALREADY_LOGGED_IN.CODE ->
                        {
                            SopoLog.d(msg = "Already_logged_in")

                            val jwtToken = apiResult.data as String

                            validateResult.postValue(
                                ValidateResult(
                                    false,
                                    CodeUtil.returnCodeMsg(apiResult.code),
                                    jwtToken,
                                    InfoConst.CUSTOM_DIALOG
                                )
                            )
                        }
                        else ->
                        {
                            validateResult.postValue(
                                ValidateResult(
                                    false,
                                    CodeUtil.returnCodeMsg(exception.data()?.code),
                                    null,
                                    InfoConst.CUSTOM_DIALOG
                                )
                            )
                        }
                    }
                }
            }

        }
    }
/*
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
                                ResponseCodeEnum.SUCCESS.CODE ->
                                {
                                    SopoLog.d(
                                        tag = TAG,
                                        msg = "What the fuck ${result.data.toString()}"
                                    )
                                    val gson = Gson()

                                    val type = object : TypeToken<LoginResult?>()
                                    {}.type

                                    val reader = gson.toJson(result.data)

                                    val user = gson.fromJson<LoginResult>(reader, type)

                                    userRepoImpl.setEmail(email = user.userName)
                                    userRepoImpl.setApiPwd(pwd = user.password)
                                    userRepoImpl.setDeviceInfo(info = deviceInfo)
                                    userRepoImpl.setJoinType(joinType = joinType)
                                    userRepoImpl.setRegisterDate(user.regDt)
                                    userRepoImpl.setStatus(user.status)

                                    validateResult.value =
                                        ValidateResult(
                                            true,
                                            result.message,
                                            result,
                                            InfoConst.NON_SHOW
                                        )
                                }
                                ResponseCodeEnum.ALREADY_LOGGED_IN.CODE ->
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

 */
}