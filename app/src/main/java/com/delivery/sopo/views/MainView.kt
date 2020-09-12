package com.delivery.sopo.views

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.delivery.sopo.R
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.database.room.RoomActivate
import com.delivery.sopo.databinding.MainViewBinding
import com.delivery.sopo.interfaces.BasicView
import com.delivery.sopo.models.entity.CourierEntity
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.UserAPI
import com.delivery.sopo.repository.UserRepo
import com.delivery.sopo.viewmodels.MainViewModel
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers.io
import org.koin.android.ext.android.inject

class MainView : BasicView<MainViewBinding>(R.layout.main_view)
{
    lateinit var mainVm: MainViewModel
    private val userRepo: UserRepo by inject()

    private var transaction: FragmentTransaction? = null

    init
    {
        TAG += "MainView"
        transaction = supportFragmentManager.beginTransaction()
        NetworkManager.initPrivateApi(id = userRepo.getEmail(), pwd = userRepo.getApiPwd())
        Log.d(TAG, "ID = ${userRepo.getEmail()}")
        Log.d(TAG, "ID = ${userRepo.getApiPwd()}")
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        RoomActivate.initCourierDB(this@MainView)
        updateFCMToken()
    }

    private fun updateFCMToken()
    {
        FirebaseInstanceId.getInstance().instanceId.addOnCompleteListener { task ->

            val token = task.result!!.token

            Log.d(TAG, "FCM - $token")

            NetworkManager.privateRetro.create(UserAPI::class.java)
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

    override fun bindView()
    {
        mainVm = MainViewModel(object : MainViewModel.MainActivityContract
        {
            override fun getFragmentManger(): FragmentManager = supportFragmentManager
        })

        binding.vm = mainVm
        binding.executePendingBindings()
    }

    override fun setObserver()
    {
    }
}