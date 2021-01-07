package com.delivery.sopo.viewmodels.login

import android.text.TextUtils
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.enums.ResponseCodeEnum
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.extensions.commonMessageResId
import com.delivery.sopo.firebase.FirebaseManagementImpl
import com.delivery.sopo.mapper.OauthMapper
import com.delivery.sopo.models.*
import com.delivery.sopo.networks.api.LoginAPICall
import com.delivery.sopo.networks.api.UserAPICall
import com.delivery.sopo.networks.dto.JsonPatchDto
import com.delivery.sopo.repository.impl.OauthRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.Result
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.ValidateUtil
import com.delivery.sopo.viewmodels.signup.FocusChangeCallback
import com.delivery.sopo.views.widget.CustomEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(val userRepoImpl: UserRepoImpl, val oauthRepoImpl: OauthRepoImpl) : ViewModel()
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
    private var _result = MutableLiveData<TotalResult<*>?>()
    val result: LiveData<TotalResult<*>?>
        get() = _result

    val isProgress = MutableLiveData<Boolean?>()

    init
    {
        setInitUI()
        _result.value = TotalResult<String>(null, null)
    }

    // UI 초기화
    private fun setInitUI()
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
    private fun onCheckValidate(): TotalResult<*>
    {
        SopoLog.d(msg = "onCheckValidate call()")
        // email 유효성 에러
        if (isEmailCorVisible.value != View.VISIBLE)
        {
            SopoLog.d(tag = TAG, msg = "Validate Fail Email ")
            emailStatusType.value = CustomEditText.STATUS_COLOR_RED
            return TotalResult<Unit>(
                errorResult = ErrorResult(
                    codeEnum = null,
                    errorMsg = emailValidateText.value.toString(),
                    errorType = ErrorResult.ERROR_TYPE_NON,
                    data = null
                )
            )
        }

        // password 유효성 에러
        if (isPwdCorVisible.value != View.VISIBLE)
        {
            SopoLog.d(tag = TAG, msg = "Validate Fail PWD ")
            pwdStatusType.value = CustomEditText.STATUS_COLOR_RED
            return TotalResult<Unit>(
                errorResult = ErrorResult(
                    codeEnum = null,
                    errorMsg = pwdValidateText.value.toString(),
                    errorType = ErrorResult.ERROR_TYPE_NON,
                    data = null
                )
            )
        }

        return TotalResult<Unit>(
            successResult = SuccessResult(
                codeEnum = null,
                successMsg = "SUCCESS",
                data = null
            )
        )
    }

    fun onLoginClicked(v: View)
    {
        SopoLog.d(tag = TAG, msg = "onLoginClicked() call!!!")

        _result.value = onCheckValidate()

        // result가 전부 통과일 때
        if (_result.value?.successResult != null)
        {
            isProgress.postValue(true)

            // Firebase email login
            FirebaseManagementImpl.firebaseSelfLogin(email = email.value!!, pwd = pwd.value!!) {

                // 성공일 때
                if (it.isSuccessful)
                {
                    SopoLog.d(tag = TAG, msg = "Firebase Login")

                    // Firebase email 인증 통과
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
                    // email 인증 실패
                    else
                    {
                        isProgress.postValue(false)
                        SopoLog.d(tag = TAG, msg = "Not Auth Email ===> ${it.result.user?.uid}")
                        _result.postValue(
                            TotalResult(
                                errorResult = ErrorResult<String?>(
                                    null,
                                    "이메일 인증을 해주세요.",
                                    ErrorResult.ERROR_TYPE_DIALOG,
                                    null,
                                    it.exception
                                )
                            )
                        )
                    }
                }
                else
                {
                    isProgress.postValue(false)
                    SopoLog.d(tag = TAG, msg = "Firebase Login Fail ${it.exception?.message}")
                    _result.value = TotalResult<Int>(
                        errorResult = ErrorResult(
                            codeEnum = null,
                            errorMsg = it.exception?.message ?: "",
                            errorType = ErrorResult.ERROR_TYPE_DIALOG,
                            data = it.exception?.commonMessageResId,
                            e = it.exception
                        )
                    )

                }
            }
        }
        // result가 에러일 때
        else if (_result.value?.errorResult != null)
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
        else
        {
            SopoLog.d(msg = "Total Result All NULL")
        }

    }

    private fun requestLoginWithOauth(email: String, password: String, deviceInfo: String)
    {
        SopoLog.d(tag = TAG, msg = "requestLoginWithOauth() call!!!")

        CoroutineScope(Dispatchers.IO).launch {

            when (val result = LoginAPICall().requestOauth(email, deviceInfo, password))
            {
                is Result.Success ->
                {
                    SopoLog.d(msg = "requestOauth Success => ${result}")

                    // data를 gson라이브러리로 타입 변환
                    val gson = Gson()
                    val type = object : TypeToken<OauthResult>()
                    {}.type
                    val reader = gson.toJson(result.data)
                    val data = gson.fromJson<OauthResult>(reader, type)

                    val oauthData = OauthMapper.objectToEntity(email, data)


                    // accessToken 기한이 0일 때
                    if (oauthData.expiresIn == "0")
                    {
                        requestRefreshOauth(
                            email,
                            data.refreshToken,
                            OtherUtil.getDeviceID(SOPOApp.INSTANCE)
                        )
                    }
                    else
                    {
                        withContext(Dispatchers.Default) {
                            oauthRepoImpl.insert(oauthData)
                        }

                        userRepoImpl.setEmail(email = email)
                        userRepoImpl.setDeviceInfo(info = deviceInfo)
                        userRepoImpl.setJoinType(joinType = "self")
                        userRepoImpl.setStatus(1)

                        _result.postValue(
                            TotalResult<Result<Any>>(
                                successResult = SuccessResult(
                                    codeEnum = ResponseCodeEnum.SUCCESS,
                                    successMsg = "SUCCESS",
                                    data = result
                                )
                            )
                        )

                        isProgress.postValue(false)
                    }
                }
                is Result.Error ->
                {
                    isProgress.postValue(false)

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

                            _result.postValue(
                                TotalResult<String>(
                                    errorResult = ErrorResult(
                                        codeEnum = ResponseCodeEnum.ALREADY_LOGGED_IN,
                                        errorMsg = CodeUtil.returnCodeMsg(apiResult.code),
                                        data = jwtToken,
                                        errorType = ErrorResult.ERROR_TYPE_DIALOG,
                                        e = exception
                                    )
                                )
                            )
                        }
                        else ->
                        {
                            _result.postValue(
                                TotalResult<String?>(
                                    errorResult = ErrorResult(
                                        codeEnum = null,
                                        errorMsg = CodeUtil.returnCodeMsg(apiResult?.code),
                                        data = null,
                                        errorType = ErrorResult.ERROR_TYPE_DIALOG,
                                        e = exception
                                    )
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

                    val oauthData = OauthMapper.objectToEntity(email, data)

                    withContext(Dispatchers.Default) {
                        oauthRepoImpl.insert(oauthData)
                    }

                    userRepoImpl.setEmail(email = email)
                    userRepoImpl.setDeviceInfo(info = deviceInfo)
                    userRepoImpl.setJoinType(joinType = "self")
                    userRepoImpl.setStatus(1)

                    _result.postValue(
                        TotalResult<Result<Any>>(
                            successResult = SuccessResult(
                                codeEnum = ResponseCodeEnum.SUCCESS,
                                successMsg = "SUCCESS",
                                data = result
                            )
                        )
                    )

                }
                is Result.Error ->
                {
                    isProgress.postValue(false)
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

                            _result.postValue(
                                TotalResult<String>(
                                    errorResult = ErrorResult(
                                        codeEnum = ResponseCodeEnum.ALREADY_LOGGED_IN,
                                        errorMsg = CodeUtil.returnCodeMsg(apiResult.code),
                                        data = jwtToken,
                                        errorType = ErrorResult.ERROR_TYPE_DIALOG,
                                        e = exception
                                    )
                                )
                            )
                        }
                        else ->
                        {
                            _result.postValue(
                                TotalResult<String?>(
                                    errorResult = ErrorResult(
                                        codeEnum = null,
                                        errorMsg = CodeUtil.returnCodeMsg(apiResult?.code),
                                        data = null,
                                        errorType = ErrorResult.ERROR_TYPE_DIALOG,
                                        e = exception
                                    )
                                )
                            )
                        }
                    }
                }
            }

        }
    }

    fun authJwtToken(jwtToken: String)
    {
        isProgress.postValue(true)
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
                        isProgress.postValue(false)

                        _result.postValue(
                            TotalResult<String?>(
                                errorResult = ErrorResult(
                                    codeEnum = null,
                                    errorMsg = CodeUtil.returnCodeMsg(apiResult.code),
                                    data = null,
                                    errorType = ErrorResult.ERROR_TYPE_DIALOG,
                                    e = null
                                )
                            )
                        )
                    }
                }
                is Result.Error ->
                {
                    isProgress.postValue(false)

                    val exception = result.exception as APIException
                    val apiResult = exception.data()

                    _result.postValue(
                        TotalResult<String?>(
                            errorResult = ErrorResult(
                                codeEnum = null,
                                errorMsg = CodeUtil.returnCodeMsg(apiResult?.code),
                                data = null,
                                errorType = ErrorResult.ERROR_TYPE_DIALOG,
                                e = exception
                            )
                        )
                    )
                }
            }
        }
    }
}