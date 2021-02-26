package com.delivery.sopo.thirdpartyapi.kako

import android.util.Log
import com.delivery.sopo.util.SopoLog
import com.kakao.auth.ISessionCallback
import com.kakao.network.ErrorResult
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import com.kakao.util.exception.KakaoException
import java.util.*

class SessionCallback : ISessionCallback {
    var TAG = "LOG.SOPO"

    override fun onSessionOpened() {
//        requestKakaoLogin()
    }

    override fun onSessionOpenFailed(exception: KakaoException) {
        if (exception != null) {
            SopoLog.d( msg = "exception : $exception")
        }
    }

    /** 사용자에 대한 정보를 가져온다  */
    private fun requestMe(cb: (Any) -> Unit) {
        val keys: MutableList<String> =
            ArrayList()
        keys.add("kakao_account.email")
        UserManagement.getInstance().me(keys, object : MeV2ResponseCallback() {
            override fun onFailure(errorResult: ErrorResult) {
                super.onFailure(errorResult)
                cb.invoke(errorResult)
                Log.e(
                    TAG,
                    "requestKakaoLogin onFailure message : " + errorResult.errorMessage
                )
            }

            override fun onFailureForUiThread(errorResult: ErrorResult) {
                super.onFailureForUiThread(errorResult)
                cb.invoke(errorResult)
                Log.e(
                    TAG,
                    "requestKakaoLogin onFailureForUiThread message : " + errorResult.errorMessage
                )
            }

            override fun onSessionClosed(errorResult: ErrorResult) {
                cb.invoke(errorResult)
                Log.e(
                    TAG,
                    "requestKakaoLogin onSessionClosed message : " + errorResult.errorMessage
                )
            }

            override fun onSuccess(result: MeV2Response) {
                cb.invoke(result)
            }
        })
    }
}