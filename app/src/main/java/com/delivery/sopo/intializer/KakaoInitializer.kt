package com.delivery.sopo.intializer

import android.content.Context
import androidx.startup.Initializer
import com.delivery.sopo.R
import com.kakao.sdk.common.KakaoSdk

/**
 * Logger 기본 세팅. 초기 시작 시 동작되도록 InitializationProvider에 설정
 *
 */
class KakaoInitializer : Initializer<Unit> {

    override fun create(context: Context) {
        KakaoSdk.init(context, context.getString(R.string.KAKAO_API_KEY))
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}