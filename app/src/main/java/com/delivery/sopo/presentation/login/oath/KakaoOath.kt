package com.delivery.sopo.presentation.login.oath

import android.content.Context
import com.kakao.sdk.user.UserApiClient
import com.kakao.sdk.user.model.User

class KakaoOath(private val context: Context)
{
    fun signIn(onSuccess:(User)->Unit, onFailure:(Throwable)->Unit) {
        if (!isUsableAppLogin(context)) requestKakaoWebLogin(context, onSuccess, onFailure)
        else requestKakaoAppLogin(context, onSuccess, onFailure)
    }

    /**
     * Request kakao app login
     *
     * Device 내 카카오톡 Application으로 로그인할 수 있는 경우
     */
    private fun requestKakaoAppLogin(context: Context, onSuccess:(User)->Unit, onFailure:(Throwable)->Unit) {
        UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->

            if(error != null) return@loginWithKakaoTalk onFailure(error)

            UserApiClient.instance.me { user, error ->
                if(error != null) return@me onFailure(error)
                onSuccess(user?: return@me onFailure(Exception("USER NOT EXIST")))
            }
        }
    }

    /**
     * Request kakao web login
     *
     * Device 내 카카오톡 Application으로 로그인하지 못하는 경우, Web 호출
     *
     * @param context
     */
    private fun requestKakaoWebLogin(context: Context, onSuccess:(User)->Unit, onFailure:(Throwable)->Unit) {
        UserApiClient.instance.loginWithKakaoAccount(context) { token, error ->
            if(error != null) return@loginWithKakaoAccount onFailure(error)

            UserApiClient.instance.me { user, error ->
                if(error != null) return@me onFailure(error)
                onSuccess(user?: return@me onFailure(Exception("USER NOT EXIST")))
            }
        }
    }

    /**
     * Is usable kakao login
     *
     * 카카오톡(Application)으로 로그인 가능(설치) 여부
     */
    private fun isUsableAppLogin(context: Context): Boolean {
        return UserApiClient.instance.isKakaoTalkLoginAvailable(context)
    }
}