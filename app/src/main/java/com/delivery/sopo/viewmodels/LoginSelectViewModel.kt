package com.delivery.sopo.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.networks.NetworkManager
import com.kakao.auth.Session
import com.kakao.auth.authorization.accesstoken.AccessToken
import com.kakao.network.ErrorResult
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.callback.UnLinkResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import io.reactivex.schedulers.Schedulers
import java.util.ArrayList

class LoginSelectViewModel : ViewModel() {

    val TAG = "LOG.SOPO.LoginSelectVm"

    val loginType = MutableLiveData<String>()

    fun onGoLoginClicked() {
        loginType.value = "LOGIN"
    }
    fun onGoSignUpClicked() {
        loginType.value = "SIGN_UP"
    }
    fun onKakaoLoginClicked() {


        loginType.value = "KAKAO_LOGIN"
    }


    /** 사용자에 대한 정보를 가져온다  */
    fun requestMe(cb: (Any) -> Unit) {

        val keys: MutableList<String> =
            ArrayList()
        keys.add("kakao_account.email")
        UserManagement.getInstance().me(keys, object : MeV2ResponseCallback() {
            override fun onFailure(errorResult: ErrorResult) {
                super.onFailure(errorResult)
                cb.invoke(errorResult)
                Log.e(
                    TAG,
                    "requestMe onFailure message : " + errorResult.errorMessage
                )
            }

            override fun onFailureForUiThread(errorResult: ErrorResult) {
                super.onFailureForUiThread(errorResult)
                cb.invoke(errorResult)
                Log.e(
                    TAG,
                    "requestMe onFailureForUiThread message : " + errorResult.errorMessage
                )
            }

            override fun onSessionClosed(errorResult: ErrorResult) {
                cb.invoke(errorResult)
                Log.e(
                    TAG,
                    "requestMe onSessionClosed message : " + errorResult.errorMessage
                )
            }

            override fun onSuccess(result: MeV2Response) {
                cb.invoke(result)
            }
        })
    }
}