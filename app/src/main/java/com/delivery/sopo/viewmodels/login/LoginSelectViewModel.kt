package com.delivery.sopo.viewmodels.login

import android.content.Intent
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.JoinTypeConst
import com.delivery.sopo.enums.ResponseCodeEnum
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.firebase.FirebaseManagementImpl
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.LoginResult
import com.delivery.sopo.networks.api.LoginAPICall
import com.delivery.sopo.networks.api.UserAPICall
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.Result
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.views.main.MainView
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class LoginSelectViewModel(private val userRepoImpl: UserRepoImpl) : ViewModel()
{
    val TAG = "LoginSelectVm"

    var email = ""
    var deviceInfo = OtherUtil.getDeviceID(SOPOApp.INSTANCE)
    var kakaoUserId = ""
    var firebaseUserId = ""

    val loginType = MutableLiveData<String>()
    var bgImg = MutableLiveData<Int>()

    // 에러 메시지 송출 todo 추후 에러코드 정의할 때 에러코드 및 문구를 묶어서 전송
    private var _errorResult = MutableLiveData<ErrorResult<Any>?>()
    val errorResult: LiveData<ErrorResult<Any>?>
        get() = _errorResult

    // todo 성공에 대한 객체 만들어서 통일 시키기 0: 정상 로그인, 1: 중복 로그인
    private var _successResult = MutableLiveData<Pair<Int, String>?>()
    val successResult: LiveData<Pair<Int, String>?>
        get() = _successResult

    init
    {
        bgImg.value = R.drawable.ic_login_ani_box
    }

    fun onGoLoginClicked()
    {
        loginType.value = "LOGIN"
    }

    fun onGoSignUpClicked()
    {
        loginType.value = "SIGN_UP"
    }

    fun onKakaoLoginClicked()
    {
        loginType.value = "KAKAO_LOGIN"
    }

    // 사용자에 대한 정보를 가져온다

    fun requestMe()
    {
        SopoLog.d(msg = "requestMe Call()")

        val keys: MutableList<String> = ArrayList()
        keys.add("kakao_account.email")

        UserManagement.getInstance().me(keys, object : MeV2ResponseCallback()
        {
            override fun onFailure(errorResult: com.kakao.network.ErrorResult)
            {
                super.onFailure(errorResult)

                SopoLog.e(
                    tag = TAG,
                    msg = "requestMe onFailure message : " + errorResult.errorMessage,
                    e = null
                )
                _errorResult.value = ErrorResult(
                    errorMsg = errorResult.errorMessage,
                    errorType = ErrorResult.ERROR_TYPE_DIALOG,
                    data = errorResult,
                    e = errorResult.exception
                )
            }

            override fun onFailureForUiThread(errorResult: com.kakao.network.ErrorResult)
            {
                super.onFailureForUiThread(errorResult)
                SopoLog.e(
                    tag = TAG,
                    msg = "requestMe onFailureForUiThread message : " + errorResult.errorMessage,
                    e = null
                )

                _errorResult.value = ErrorResult(
                    errorMsg = errorResult.errorMessage,
                    errorType = ErrorResult.ERROR_TYPE_DIALOG,
                    data = errorResult,
                    e = errorResult.exception
                )
            }

            override fun onSessionClosed(errorResult: com.kakao.network.ErrorResult)
            {
                SopoLog.e(
                    tag = TAG,
                    msg = "requestMe onSessionClosed message : " + errorResult.errorMessage,
                    e = null
                )

                _errorResult.value = ErrorResult(
                    errorMsg = errorResult.errorMessage,
                    errorType = ErrorResult.ERROR_TYPE_DIALOG,
                    data = errorResult,
                    e = errorResult.exception
                )
            }

            override fun onSuccess(result: MeV2Response)
            {
                email = result.kakaoAccount.email
                kakaoUserId = result.id.toString()

                requestKakaoCustomToken(email = email, uid = kakaoUserId)
            }
        })
    }

    /*
    fun requestMe(cb: (Any) -> Unit)
    {
        SopoLog.d(msg = "requestMe Call()")

        val keys: MutableList<String> = ArrayList()
        keys.add("kakao_account.email")

        UserManagement.getInstance().me(keys, object : MeV2ResponseCallback()
        {
            override fun onFailure(errorResult: com.kakao.network.ErrorResult)
            {
                super.onFailure(errorResult)

                SopoLog.e(
                    tag = TAG,
                    msg = "requestMe onFailure message : " + errorResult.errorMessage,
                    e = null
                )
                _errorResult.value =
                    ErrorResult(codeEnum = null, errorMsg = errorResult.errorMessage, errorType = ErrorResult.ERROR_TYPE_DIALOG, e= null)

                cb.invoke(errorResult)
            }

            override fun onFailureForUiThread(errorResult: com.kakao.network.ErrorResult)
            {
                super.onFailureForUiThread(errorResult)
                SopoLog.e(
                    tag = TAG,
                    msg = "requestMe onFailureForUiThread message : " + errorResult.errorMessage,
                    e = null
                )

                _errorResult.value =
                    ErrorResult(codeEnum = null, errorMsg = errorResult.errorMessage, errorType = ErrorResult.ERROR_TYPE_DIALOG, e= null)

                cb.invoke(errorResult)
            }

            override fun onSessionClosed(errorResult: com.kakao.network.ErrorResult)
            {
                SopoLog.e(
                    tag = TAG,
                    msg = "requestMe onSessionClosed message : " + errorResult.errorMessage,
                    e = null
                )
                cb.invoke(errorResult)
            }

            override fun onSuccess(result: MeV2Response)
            {
                cb.invoke(result)
            }
        })
    }
    */
    fun requestKakaoCustomToken(email: String, uid: String)
    {
        CoroutineScope(Dispatchers.IO).launch {
            val result = UserAPICall().requestCustomToken(
                email = email,
                deviceInfo = deviceInfo,
                joinType = JoinTypeConst.KAKAO,
                userId = uid
            )

            when (result)
            {
                is Result.Success ->
                {
                    if (result.statusCode == 200)
                    {
                        val customToken = result.data.data!!

                        FirebaseManagementImpl.firebaseCustomTokenLogin(token = customToken) { task ->
                            if (!task.isSuccessful)
                            {
                                _errorResult.value = ErrorResult(
                                    errorMsg = "Fail To Firebase Custom Login",
                                    errorType = ErrorResult.ERROR_TYPE_DIALOG,
                                    e = task.exception
                                )
                                return@firebaseCustomTokenLogin
                            }
                            else
                            {
                                firebaseUserId = task.result.user?.uid ?: ""

                                if (firebaseUserId == "")
                                {
                                    _errorResult.value = ErrorResult(
                                        errorMsg = "Fail To Get Firebase uid",
                                        errorType = ErrorResult.ERROR_TYPE_DIALOG,
                                        e = task.exception
                                    )
                                    return@firebaseCustomTokenLogin
                                }

                                // firebase에 email이 등록되어있지 않다면 또는 등록된 이메일과 다르다면 새로 email을 업데이트한다.
                                if (task.result.user?.email == null || task.result.user?.email == "" || task.result.user?.email == email)
                                {
                                    requestFirebaseUpdateEmail(task)
                                }
                                else
                                {
                                    // customToken 로그인 시도
                                    requestKakaoLogin(email, deviceInfo, kakaoUserId, uid)
                                }


                            }
                        }
                    }
                    else
                    {
                        _errorResult.value = ErrorResult(
                            errorMsg = CodeUtil.returnCodeMsg(result.data.code),
                            errorType = ErrorResult.ERROR_TYPE_DIALOG,
                            e = null
                        )
                    }
                }
                is Result.Error ->
                {
                    val exception = result.exception as APIException

                    if (exception.errorBody != null)
                    {

                    }


                }
            }
        }
    }

    private fun requestFirebaseUpdateEmail(task: Task<AuthResult>)
    {
        FirebaseManagementImpl.firebaseUpdateEmail(email, task) { task2 ->
            if (task2 == null)
            {
                _errorResult.value = ErrorResult(
                    errorMsg = "Fail To Update Firebase email",
                    errorType = ErrorResult.ERROR_TYPE_DIALOG,
                    e = task2?.exception
                )
                return@firebaseUpdateEmail
            }
            else
            {
                if (!task2.isSuccessful)
                {
                    _errorResult.value = ErrorResult(
                        errorMsg = "Fail To Update Firebase email",
                        errorType = ErrorResult.ERROR_TYPE_DIALOG,
                        e = task2.exception
                    )

                    return@firebaseUpdateEmail
                }
                else
                {
                    requestKakaoLogin(
                        email = email,
                        deviceInfo = deviceInfo,
                        kakaoUserId = kakaoUserId,
                        uid = firebaseUserId
                    )
                }
            }
        }
    }

    fun requestKakaoLogin(
        email: String,
        deviceInfo: String,
        kakaoUserId: String,
        uid: String
    )
    {
        CoroutineScope(Dispatchers.IO).launch {
            val result = LoginAPICall().requestKakaoJoin(email, kakaoUserId, deviceInfo, kakaoUserId, uid)

            when (result)
            {
                is Result.Success ->
                {
                    SopoLog.e(msg = "OnSuccess ${result}")

                }
                is Result.Error ->
                {
                    SopoLog.e(msg = "error Code => ${result}", e = result.exception)
                }
            }
        }
/*
        CoroutineScope(Dispatchers.IO).launch {
            val result = LoginAPICall().requestKakaoLogin(
                email = email,
                deviceInfo = deviceInfo,
                kakaoUserId = kakaoUserId,
                uid = uid
            )

            when (result)
            {
                is Result.Success ->
                {
                    val apiResult = result.data

                    SopoLog.d(msg = apiResult.toString())
                    SopoLog.d(msg = "${result.statusCode}")

                    when (apiResult.code)
                    {
                        ResponseCodeEnum.SUCCESS.CODE ->
                        {
                            val gson = Gson()
                            val type = object : TypeToken<LoginResult?>()
                            {}.type
                            val reader = gson.toJson(apiResult.data)
                            val user = gson.fromJson<LoginResult>(reader, type)

                            SopoLog.d(msg = "User => ${user}")

                            userRepoImpl.setEmail(email = user.userName)
                            userRepoImpl.setApiPwd(pwd = user.password)
                            userRepoImpl.setDeviceInfo(info = deviceInfo)
                            userRepoImpl.setJoinType(joinType = JoinTypeConst.KAKAO)
                            userRepoImpl.setRegisterDate(user.regDt)
                            userRepoImpl.setStatus(user.status)
                            userRepoImpl.setSNSUId(kakaoUserId)
                            userRepoImpl.setUserNickname(user.userNickname ?: "")

                            _successResult.postValue(Pair(0, "success"))
                        }
                        ResponseCodeEnum.ALREADY_LOGGED_IN.CODE ->
                        {
                            val jwtToken = apiResult.data as String
                            _successResult.postValue(Pair(1, jwtToken))
//                            GeneralDialog(
//                                act = parentActivity,
//                                title = "알림",
//                                msg = ResponseCodeEnum.ALREADY_LOGGED_IN.MSG,
//                                detailMsg = null,
//                                rHandler = Pair(
//                                    first = "네",
//                                    second = { it ->
//                                        it.dismiss()
//                                        updateDeviceInfo(jwtToken = jwtToken, email = email)
//                                    })
//                            ).show(supportFragmentManager, "tag")
                        }
                        else ->
                        {

                        }
                    }

                }
                is Result.Error ->
                {
                    SopoLog.e(msg = "error Code => ${result.statusCode ?: 0}", e = result.exception)
                }
            }
        }

 */
    }
}