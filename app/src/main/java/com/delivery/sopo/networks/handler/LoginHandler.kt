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
import com.delivery.sopo.models.SuccessResult
import com.delivery.sopo.networks.api.LoginAPICall
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
//                                    oAuthLogin(email, kakaoUid, deviceInfo) { success, error ->
//                                        callback.invoke(success, error)
//                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

}