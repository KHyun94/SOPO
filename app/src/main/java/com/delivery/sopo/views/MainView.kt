package com.delivery.sopo.views

import android.os.Bundle
import android.util.Log
import androidx.core.view.get
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.delivery.sopo.R
import com.delivery.sopo.databinding.MainViewBinding
import com.delivery.sopo.interfaces.BasicView
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.UserRepo
import com.delivery.sopo.viewmodels.MainViewModel
import com.google.android.material.tabs.TabLayout
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers.io
import org.koin.android.ext.android.inject

class MainView : BasicView<MainViewBinding>(R.layout.main_view)
{
    lateinit var mainVm: MainViewModel
//    private val mainVm: MainViewModel by inject()
    private val userRepo: UserRepo by inject()

    private var transaction: FragmentTransaction? = null

    init
    {
        TAG += "MainView"
        transaction = supportFragmentManager.beginTransaction()
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
    }

    fun firebaseTmpFun()
    {
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


    override fun bindView()
    {
        mainVm  = MainViewModel(object : MainViewModel.MainActivityContract {
            override fun getFragmentManger(): FragmentManager = supportFragmentManager
        })

        binding.vm = mainVm
        binding.executePendingBindings()
    }

    override fun setObserver()
    {
//        binding.vm?.currentTabName?.observe(this@MainView, Observer {
//            when (it)
//            {
//                FragmentType.REGISTER_STEP1.NAME ->
//                {
//                    FragmentManager.move(this@MainView, FragmentType.REGISTER_STEP1, frame_container.id)
//                }
//                FragmentType.REGISTER_STEP2.NAME ->
//                {
//                    FragmentManager.move(this@MainView, FragmentType.REGISTER_STEP2, frame_container.id)
//                }
//                FragmentType.LOOKUP.NAME ->
//                {
//                    FragmentManager.move(this@MainView, FragmentType.LOOKUP, frame_container.id)
//                }
//                FragmentType.MY_MENU.NAME ->
//                {
//                    FragmentManager.move(this@MainView, FragmentType.MY_MENU, frame_container.id)
//                }
//            }
//        })
    }
}