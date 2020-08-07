package com.tcodevice.card.tada.firebase

import com.google.firebase.remoteconfig.BuildConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.delivery.sopo.R


class FirebaseConfig private constructor()
{
    private var mFirebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
    private var isFinished = false
    private var isFetched = false

    init
    {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .build()

        mFirebaseRemoteConfig.setConfigSettings(configSettings)
        mFirebaseRemoteConfig.setDefaults(R.xml.remote_config)
    }

    fun fetch()
    {
        mFirebaseRemoteConfig
            .fetch(1)
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    mFirebaseRemoteConfig.activateFetched()
                    isFetched = true
                }
                else
                {
                }

                isFinished = true
            }
    }

    companion object
    {
        private var instance: FirebaseConfig? = null

        fun getInstance(): FirebaseConfig
        {
            if (instance == null) instance = FirebaseConfig()
            return instance as FirebaseConfig
        }
    }

}
