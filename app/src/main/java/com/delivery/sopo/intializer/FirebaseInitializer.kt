package com.delivery.sopo.intializer

import android.content.Context
import androidx.startup.Initializer
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth

/**
 * Logger 기본 세팅. 초기 시작 시 동작되도록 InitializationProvider에 설정
 *
 */
class FirebaseInitializer : Initializer<Unit>
{
    override fun create(context: Context)
    {
        //Firebase Init
        FirebaseApp.initializeApp(context)

        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.setLanguageCode("kr")
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}