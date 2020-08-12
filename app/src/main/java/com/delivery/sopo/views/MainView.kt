package com.delivery.sopo.views

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.FragmentConst.FRAGMENT_BOOK
import com.delivery.sopo.consts.FragmentConst.FRAGMENT_LOOKUP
import com.delivery.sopo.consts.FragmentConst.FRAGMENT_MY_INFO
import com.delivery.sopo.consts.FragmentConst.FRAGMENT_REGISTER
import com.delivery.sopo.databinding.MainViewBinding
import com.delivery.sopo.enums.FragmentType
import com.delivery.sopo.interfaces.BasicView
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.UserRepo
import com.delivery.sopo.util.ui_util.FragmentManager
import com.delivery.sopo.viewmodels.MainViewModel
import com.google.firebase.iid.FirebaseInstanceId
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers.io
import kotlinx.android.synthetic.main.main_view.*
import org.koin.android.ext.android.inject

class MainView : BasicView<MainViewBinding>(R.layout.main_view)
{
    private val mainVm: MainViewModel by inject()
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
        binding.vm = mainVm
        binding.executePendingBindings()
    }

    override fun setObserver()
    {
        binding.vm?.currentTabName?.observe(this@MainView, Observer {
            when (it)
            {
                FRAGMENT_REGISTER ->
                {
                    FragmentManager.move(this@MainView, FragmentType.REGISTER, frame_container.id)
//                    Log.d(TAG, FRAGMENT_REGISTER)
                }
                FRAGMENT_LOOKUP ->
                {
                    FragmentManager.move(this@MainView, FragmentType.LOOKUP, frame_container.id)
//                    Log.d(TAG, FRAGMENT_LOOKUP)
                }
                FRAGMENT_BOOK ->
                {

                }
                FRAGMENT_MY_INFO ->
                {

                }
            }
        })
    }
}