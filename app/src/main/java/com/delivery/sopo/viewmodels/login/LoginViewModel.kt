package com.delivery.sopo.viewmodels.login

import android.os.Handler
import android.view.View
import androidx.annotation.ColorRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.bindings.FocusChangeCallback
import com.delivery.sopo.consts.InfoConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.enums.InfoEnum
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
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginViewModel(val userRepoImpl: UserRepoImpl, val oAuthRepo: OauthRepoImpl): ViewModel()
{
    val email = MutableLiveData<String>()
    val emailErrorMessage = MutableLiveData<String?>()

    var password = MutableLiveData<String>()

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

    private val _focus = MutableLiveData<Triple<View, Boolean, InfoEnum>>()
    val focus: MutableLiveData<Triple<View, Boolean, InfoEnum>>
    get() = _focus

    val focusChangeCallback: FocusChangeCallback = object : FocusChangeCallback{
        override fun invoke(v: View, hasFocus: Boolean, type: InfoEnum)
        {
            SopoLog.i("${type.NAME} >>> $hasFocus")
            Handler().postDelayed(Runnable { _focus.value = (Triple(v, hasFocus, type)) }, 50)
        }

    }

    fun focusIn(type: InfoEnum)
    {
        SopoLog.d("${type}::focus in")

        /**
         * progress,
         * BoxBackground -> GRAY_50
         * ErrorMessage off
         * endIcon -> clearMark
         *         -> clearEvent
         */
        when(type)
        {
            InfoEnum.EMAIL ->
            {

            }
            InfoEnum.PASSWORD ->
            {

            }
        }
    }

    fun focusOut(type: InfoEnum)
    {
        SopoLog.d("${type}::focus out")

        when(type)
        {
            InfoEnum.EMAIL ->
            {
                /**
                 * if validate is failed,
                 * BoxBackground -> GRAY_50
                 * ErrorMessage on
                 * endIcon -> errorMark
                 */


                /**
                 * if validate is succeed,
                 * BoxBackground -> MAIN_BLUE_50
                 * ErrorMessage off
                 * endIcon -> errorMark
                 * endIcon -> successMark
                 */

            }
            InfoEnum.PASSWORD ->
            {

            }
        }
    }

    init
    {
        setInitValue()
    }

    // UI 초기화
    private fun setInitValue()
    {
        email.value = ""
        password.value = ""
    }

    fun onLoginClicked(v: View)
    {
        try
        {
            SopoLog.d(msg = "onLoginClicked() call!!!")

//            v.requestFocusFromTouch()

            if(!(ValidateUtil.isValidateEmail(email = email.value.toString())))
            {
                _result.postValue(ResponseResult(false, null, null, "이메일 양식을 확인해주세요.", DisplayEnum.DIALOG))
                return
            }

            if(!ValidateUtil.isValidatePassword(password.value.toString()))
            {
                _result.postValue(ResponseResult(false, null, null, "비밀번호 형식을 확인해주세요.", DisplayEnum.DIALOG))
                return
            }

            // result가 전부 통과일 때
            _isProgress.postValue(true)

            // 성공
            CoroutineScope(Dispatchers.IO).launch {
                loginWithOAuth(email.value.toString(), password.value.toString())
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
                _result.postValue(ResponseResult(false, code, Unit, exception.errorMessage, DisplayEnum.DIALOG))
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