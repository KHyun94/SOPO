package com.delivery.sopo.networks.handler

import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.JoinTypeConst
import com.delivery.sopo.extensions.md5
import com.delivery.sopo.models.ErrorResult
import com.delivery.sopo.models.SuccessResult
import com.delivery.sopo.networks.repository.JoinRepository
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent

typealias LoginCallback = (SuccessResult<String?>?, ErrorResult<String?>?) -> Unit

// todo LoginHandler를 순수 통신 부분과 프로세스 연결 부분을 분리 예정
object LoginHandler : KoinComponent
{
    /**
     * 카카오 로그인
     *
     *
     */
    fun requestLoginByKakao(email : String, kakaoUid : String, nickname: String, callback : LoginCallback)
    {
        SopoLog.d( msg = "onKakaoLogin Call()")
        /**
         * kakao custom token 생성 요청
         */
        val deviceInfo = SOPOApp.deviceInfo
        val md5Hashing = kakaoUid.md5()

        //todo 임시로 kakaoUid
        CoroutineScope(Dispatchers.IO).launch {
            JoinRepository().requestJoinByKakao(email = email, password = kakaoUid, deviceInfo = deviceInfo, kakaoUid = kakaoUid, nickname = nickname)
        }
    }

}