package com.delivery.sopo.networks.handler

import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.JoinTypeConst
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.extensions.md5
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.mapper.OauthMapper
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.OauthResult
import com.delivery.sopo.models.SopoJsonPatch
import com.delivery.sopo.models.SuccessResult
import com.delivery.sopo.networks.api.LoginAPICall
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.networks.dto.JsonPatchDto
import com.delivery.sopo.networks.repository.JoinRepository
import com.delivery.sopo.networks.repository.LoginRepository
import com.delivery.sopo.repository.impl.OauthRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject

typealias LoginCallback = (SuccessResult<String?>?, ErrorResult<String?>?) -> Unit

// todo LoginHandler를 순수 통신 부분과 프로세스 연결 부분을 분리 예정
object LoginHandler : KoinComponent
{
    private val TAG = "LoginHandler"

    private val userRepoImpl : UserRepoImpl by inject()
    private val oAuthRepoImpl : OauthRepoImpl by inject()

    var email : String = ""
    var password : String = ""
    private val deviceInfo = OtherUtil.getDeviceID(SOPOApp.INSTANCE)

    fun setLogin(email : String, password : String)
    {
        this.email = email
        this.password = password
    }

    /**
     * OAuth 로그인
     * token 등의 파라미터를 반환받고 그에 문제 없을 시
     * 로그인
     * 카카오 및 자체 로그인 공통
     */
    @JvmStatic
    fun oAuthLogin(email : String, password : String, deviceInfo : String, callback : LoginCallback)
    {
        setLogin(email, password)
        CoroutineScope(Dispatchers.IO).launch {
            when (val result = LoginAPICall().requestOauth(email = email, password = password, deviceInfo = deviceInfo))
            {
                is NetworkResult.Success ->
                {
                    SopoLog.d(msg = "requestOauth Success => ${result}")

                    // 내부 디비에 저장
                    userRepoImpl.setEmail(email = email)
                    userRepoImpl.setDeviceInfo(info = deviceInfo)
                    userRepoImpl.setJoinType(joinType = JoinTypeConst.SELF)
                    userRepoImpl.setStatus(1)

                    // data를 gson라이브러리로 타입 변환
                    val gson = Gson()
                    val type = object : TypeToken<OauthResult>()
                    {}.type
                    val reader = gson.toJson(result.data)
                    val data = gson.fromJson<OauthResult>(reader, type)
                    val oauthData = OauthMapper.objectToEntity(data)

                    withContext(Dispatchers.Default) {
                        oauthData.let {
                            SOPOApp.oauth = it
                            oAuthRepoImpl.insert(it)
                        }
                    }

                    callback.invoke(
                        SuccessResult(
                            code = ResponseCode.SUCCESS, successMsg = "SUCCESS", data = "pass"
                        ), null
                    )
                }
                is NetworkResult.Error ->
                {
                    SopoLog.e(msg = "requestOauth Fail => ${result}")
                    val exception = result.exception as APIException
                    val apiResult = exception.data()
                    val code = CodeUtil.getCode(apiResult?.code)
                    SopoLog.d(msg = "API Result => ${apiResult} \n code = {${apiResult?.code}")

                    when (apiResult?.code)
                    {
                        ResponseCode.ALREADY_LOGGED_IN.CODE ->
                        {
                            SopoLog.d(msg = "Already_logged_in")

                            val jwtToken = apiResult.data as String

                            callback.invoke(
                                null, ErrorResult(
                                    code = code, errorMsg = code.MSG, data = jwtToken, errorType = ErrorResult.ERROR_TYPE_DIALOG, e = exception
                                )
                            )
                        }
                        else ->
                        {
                            callback.invoke(
                                null, ErrorResult(
                                    code = code, errorMsg = code.MSG, data = null, errorType = ErrorResult.ERROR_TYPE_DIALOG, e = exception
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * 카카오 로그인
     *
     *
     */
    fun requestLoginByKakao(email : String, kakaoUid : String, callback : LoginCallback)
    {
        SopoLog.d( msg = "onKakaoLogin Call()")
        /**
         * kakao custom token 생성 요청
         */

        CoroutineScope(Dispatchers.IO).launch {

            LoginRepository().requestCustomToken(email = email, deviceInfo = SOPOApp.deviceInfo, joinType = JoinTypeConst.KAKAO, kakaoUid = kakaoUid) { success, error ->
                if (error != null)
                {
                    callback.invoke(success, error)
                    return@requestCustomToken
                }

                if (success != null)
                {
                    val customToken = success.data ?: ""

                    /**
                     * Firebase Custom Token Login
                     */
                    FirebaseRepository.loginFirebaseWithCustomToken(email, customToken) { success, error ->
                        if (error != null)
                        {
                            callback.invoke(null, error)
                            return@loginFirebaseWithCustomToken
                        }

                        if (success != null)
                        {
                            val firebaseUId = SOPOApp.auth.currentUser?.uid ?: ""
                            val befHashingStr = firebaseUId + kakaoUid
                            val afterHashingStr = befHashingStr.md5()
                            setLogin(email, afterHashingStr)

                            //todo 임시로 kakaoUid
                            CoroutineScope(Dispatchers.IO).launch {
                                JoinRepository().requestJoinByKakao(email = email, password = kakaoUid, deviceInfo = deviceInfo, kakaoUid = kakaoUid, firebaseUid = firebaseUId) { success, error ->
                                    oAuthLogin(email, kakaoUid, deviceInfo) { success, error ->
                                        callback.invoke(success, error)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * oauth login을 호출했을 때 다른 Device에 중복 로그인 처리된
     * 사용자 정보를 update
     */
    fun authJwtToken(jwtToken : String, callback : LoginCallback)
    {
        CoroutineScope(Dispatchers.IO).launch {

            val jsonPatchList = mutableListOf<SopoJsonPatch>()
            jsonPatchList.add(
                SopoJsonPatch(
                    "replace", "/deviceInfo", OtherUtil.getDeviceID(SOPOApp.INSTANCE)
                )
            )

            when (val result =
                UserCall.patchUser(email = email, jwtToken = jwtToken, jsonPatch = JsonPatchDto(jsonPatchList)))
            {
                is NetworkResult.Success ->
                {
                    val apiResult = result.data

                    when (val code = CodeUtil.getCode(apiResult.code))
                    {
                        ResponseCode.SUCCESS ->
                        {
                            oAuthLogin(email = email, password = password, deviceInfo = deviceInfo) { success, error ->
                                callback.invoke(success, error)
                            }
                        }
                        else ->
                        {
                            callback.invoke(
                                null, ErrorResult(
                                    code = code, errorMsg = code.MSG, data = null, errorType = ErrorResult.ERROR_TYPE_DIALOG, e = null
                                )
                            )
                        }
                    }
                }
                is NetworkResult.Error ->
                {
                    val exception = result.exception as APIException
                    val apiResult = exception.data()
                    val code = CodeUtil.getCode(apiResult?.code)
                    callback.invoke(
                        null, ErrorResult(
                            code = code, errorMsg = code.MSG, data = null, errorType = ErrorResult.ERROR_TYPE_DIALOG, e = exception
                        )
                    )
                }
            }
        }
    }

    fun checkOAuth()
    {
    }
}