package com.delivery.sopo.views

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.delivery.sopo.R
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.UserRepo
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers.io
import org.koin.android.ext.android.inject

class MainView : AppCompatActivity()
{
    val TAG = "LOGLOG"
    private val userRepo : UserRepo by inject()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_view)

        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->

            val token = task.result!!.token

            NetworkManager.getPrivateUserAPI(userRepo.getEmail(), userRepo.getApiPwd())
                .updateFCMToken(userRepo.getEmail(), token)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(io())
                .subscribe(
                    {
                        Log.d(TAG, it)
                    },
                    {

                        Log.d(TAG, it.message)
                    }
                )
        }


    }
}