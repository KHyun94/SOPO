package com.delivery.sopo.viewmodels.login

import android.text.TextUtils
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.mapper.OauthMapper
import com.delivery.sopo.models.OauthResult
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.UserDetail
import com.delivery.sopo.networks.api.LoginAPICall
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.repository.impl.OauthRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.DateUtil
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

class LoginViewModel(val userRepoImpl: UserRepoImpl, val oAuthRepo: OauthRepoImpl): ViewModel()
{
    var email = MutableLiveData<String>()
    var pwd = MutableLiveData<String>()

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
    private var _result = MutableLiveData<ResponseResult<*>>()
    val result: LiveData<ResponseResult<*>>
        get() = _result

    private var _isProgress = MutableLiveData<Boolean>()
    val isProgress: LiveData<Boolean>
        get() = _isProgress

    private var _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator


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
                            type = type, errorState = View.VISIBLE, corState = View.GONE
                        )

                        /**
                         *  email, pwd 전체 유효성 검사
                         */
//                        _result.value = onCheckValidate()
                        return@FocusChangeCallback
                    }

                    // email 단독 유효성 검사
                    val isValidate = ValidateUtil.isValidateEmail(email = email.value)

                    // email 유효성 통과
                    if (isValidate)
                    {
                        emailStatusType.value = CustomEditText.STATUS_COLOR_BLUE
                        setVisibleState(
                            type = InfoConst.EMAIL, errorState = View.GONE, corState = View.VISIBLE
                        )
                    }
                    // email 유효성 에러
                    else
                    {
                        emailStatusType.value = CustomEditText.STATUS_COLOR_RED
                        emailValidateText.value = "이메일 양식을 확인해주세요."
                        setVisibleState(
                            type = type, errorState = View.VISIBLE, corState = View.GONE
                        )
                    }
//                    _result.value = onCheckValidate()
                }
                InfoConst.PASSWORD ->
                {
                    // password가 공백일 때
                    if (TextUtils.isEmpty(pwd.value))
                    {
                        pwdStatusType.value = CustomEditText.STATUS_COLOR_RED
                        pwdValidateText.value = "비밀번호를 입력해주세요."
                        setVisibleState(type, View.VISIBLE, View.GONE)
//                        _result.value = onCheckValidate()

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
                        pwdValidateText.value = "비밀번호를 확인해주세요."
                        setVisibleState(type, View.VISIBLE, View.GONE)
                    }
//                    _result.value = onCheckValidate()
                }
            }
        }
    }

    // email, password 입력 상태 확인
    private fun onCheckValidate(): ResponseResult<Unit>
    {
        SopoLog.d(msg = "onCheckValidate call()")
        // email 유효성 에러
        if (isEmailCorVisible.value != View.VISIBLE)
        {
            SopoLog.e(msg = "Fail to validate email")
            emailStatusType.value = CustomEditText.STATUS_COLOR_RED
            return ResponseResult(false, null, Unit, emailValidateText.value.toString(), DisplayEnum.TOAST_MESSAGE)
        }

        // password 유효성 에러
        if (isPwdCorVisible.value != View.VISIBLE)
        {
            SopoLog.d(msg = "Fail to validate password")
            pwdStatusType.value = CustomEditText.STATUS_COLOR_RED
            return ResponseResult(false, null, Unit, pwdValidateText.value.toString(), DisplayEnum.TOAST_MESSAGE)
        }

        return ResponseResult(true, null, Unit, "Success", DisplayEnum.NON_DISPLAY)
    }

    fun onLoginClicked(v: View)
    {
        try
        {
            SopoLog.d(msg = "onLoginClicked() call!!!")

            v.requestFocusFromTouch()

            onCheckValidate().let {
                if(!it.result)
                {
                    _result.value = it
                    return
                }
            }

            // result가 전부 통과일 때

            SopoLog.d("Firebase Login >>> ${email.value.toString()}, ${pwd.value.toString()}")

            _isProgress.postValue(true)

            // 성공
            CoroutineScope(Dispatchers.IO).launch {
                loginWithOAuth(email.value.toString(), pwd.value.toString())
            }
        }
        catch (e: Exception)
        {
            throw e
        }
    }

    fun onResetPasswordClicked()
    {
        _navigator.postValue(NavigatorConst.TO_RESET_PASSWORD)
    }

    suspend fun loginWithOAuth(email: String, password: String)
    {
        when(val result = LoginAPICall().requestOauth(email, password, SOPOApp.deviceInfo))
        {
            is NetworkResult.Success ->
            {
                userRepoImpl.setEmail(email)
                userRepoImpl.setApiPwd(password)
                userRepoImpl.setStatus(1)

                val oAuth = Gson().let {gson ->
                    val type = object : TypeToken<OauthResult>() {}.type
                    val reader = gson.toJson(result.data)
                    val data = gson.fromJson<OauthResult>(reader, type)
                    OauthMapper.objectToEntity(data)
                }

                SOPOApp.oAuthEntity = oAuth

                withContext(Dispatchers.Default){
                    oAuthRepo.insert(oAuth)
                }

                _isProgress.postValue(false)
                _result.postValue(getUserInfo())
            }
            is NetworkResult.Error ->
            {
                val exception = result.exception as APIException
                val code = exception.responseCode
                _isProgress.postValue(false)
                _result.postValue(ResponseResult(false, code, Unit, code.MSG, DisplayEnum.DIALOG))
            }
        }
    }

    suspend fun getUserInfo(): ResponseResult<UserDetail?>
    {
        val result = UserCall.getUserInfoWithToken()

        if(result is NetworkResult.Error)
        {
            val exception = result.exception as APIException
            val responseCode = exception.responseCode
            val date = SOPOApp.oAuthEntity.let { it?.expiresIn }

            return if(responseCode.HTTP_STATUS == 401 && DateUtil.isOverExpiredDate(date!!)) ResponseResult(false, responseCode, null, "로그인 기한이 만료되었습니다.\n다시 로그인해주세요.", DisplayEnum.DIALOG)
            else ResponseResult(false, responseCode, null, responseCode.MSG, DisplayEnum.DIALOG)
        }

        val apiResult = (result as NetworkResult.Success).data

        userRepoImpl.setNickname(apiResult.data?.nickname?:"")

        SopoLog.d("UserDetail >>> ${apiResult.data}, ${apiResult.data?.nickname}")

        return ResponseResult(true, ResponseCode.SUCCESS, apiResult.data, ResponseCode.SUCCESS.MSG)
    }
}