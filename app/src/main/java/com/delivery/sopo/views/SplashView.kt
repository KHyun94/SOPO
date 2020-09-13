package com.delivery.sopo.views

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.delivery.sopo.util.ui_util.GeneralDialog
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.PermissionConst
import com.delivery.sopo.databinding.SplashViewBinding
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.interfaces.BasicView
import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.LoginResult
import com.delivery.sopo.networks.LoginAPI
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.UserRepo
import com.delivery.sopo.util.fun_util.CodeUtil
import com.delivery.sopo.util.fun_util.OtherUtil
import com.delivery.sopo.views.dialog.PermissionDialog
import com.delivery.sopo.viewmodels.SplashViewModel
import com.tbruyelle.rxpermissions2.RxPermissions
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashView : BasicView<SplashViewBinding>(
    layoutRes = R.layout.splash_view
)
{
    private val userRepo: UserRepo by inject()

    private val splashVM: SplashViewModel by viewModel()
    lateinit var rxPermission: RxPermissions
    lateinit var permissionDialog: PermissionDialog

    init
    {
        TAG += this.javaClass.simpleName
        parentActivity = this@SplashView
        Log.d(TAG, "What is $TAG")

    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        rxPermission = RxPermissions.getInstance(applicationContext)
    }

    override fun bindView()
    {
        binding.vm = splashVM
        binding.executePendingBindings()
    }

    override fun setObserver()
    {
        moveToActivity()
    }

    private fun isPermissionGrant(permissionArray: Array<String>): Boolean
    {
        var isGrant = false

        for (p in permissionArray)
        {
            isGrant = ContextCompat.checkSelfPermission(
                parentActivity,
                p
            ) == PackageManager.PERMISSION_GRANTED
        }

        return isGrant
    }

    private fun moveToActivity()
    {
        binding.vm?.navigator!!.observe(this, Observer {
            when (it)
            {
                NavigatorConst.TO_PERMISSION ->
                {
                    if (!isPermissionGrant(PermissionConst.PERMISSION_ARRAY))
                    {
                        // NOT PERMISSION GRANT
                        permissionDialog = PermissionDialog(act = parentActivity) { dialog ->
                            requestPermission(PermissionConst.PERMISSION_ARRAY)
                            dialog.dismiss()
                        }

                        permissionDialog.show(supportFragmentManager, "PermissionTag")
                    }
                    else
                    {
                        binding.vm?.requestAfterActivity()
                    }
                }
                NavigatorConst.TO_INTRO ->
                {
                    startActivity(Intent(parentActivity, IntroView::class.java))
                    finish()
                }
                NavigatorConst.TO_MAIN ->
                {
                    requestAutoLogin()
                }
            }
        })
    }

    fun requestAutoLogin()
    {
        NetworkManager.initPrivateApi(userRepo.getEmail(), userRepo.getApiPwd())

        val firebaseUser = SOPOApp.auth.currentUser

        NetworkManager.privateRetro.create(LoginAPI::class.java)
            .requestAutoLogin(
                OtherUtil.getDeviceID(SOPOApp.INSTANCE),
                userRepo.getJoinType(),
                firebaseUser?.uid!!,
                userRepo.getSNSUId()
            ).enqueue(object : Callback<APIResult<LoginResult?>>
            {
                override fun onFailure(call: Call<APIResult<LoginResult?>>, t: Throwable)
                {
                    TODO("Not yet implemented")
                }

                override fun onResponse(
                    call: Call<APIResult<LoginResult?>>,
                    response: Response<APIResult<LoginResult?>>
                )
                {
                    val httpStatusCode = response.code()

                    val result = response.body()

                    when (httpStatusCode)
                    {
                        ResponseCode.SUCCESS.HTTP_STATUS ->
                        {
                            startActivity(Intent(parentActivity, MainView::class.java))
                            finish()
                        }
                        ResponseCode.INVALID_USER.HTTP_STATUS ->
                        {
                            GeneralDialog(
                                act = parentActivity,
                                title = "알림",
                                msg = "자동 로그인이 해제되었습니다.\n다시 로그인 해주세요.",
                                detailMsg = null,
                                rHandler = Pair(
                                    first = "네",
                                    second = { it ->
                                        it.dismiss()
                                        startActivity(Intent(parentActivity, IntroView::class.java))
                                        finish()
                                    }),
                                lHandler = Pair(
                                    first = "아니오",
                                    second = { it ->
                                        it.dismiss()
                                        finish()
                                    })
                            ).show(supportFragmentManager, "tag")
                        }
                        500 ->
                        {
                            GeneralDialog(
                                act = parentActivity,
                                title = "오류",
                                msg = CodeUtil.returnCodeMsg(result?.code),
                                detailMsg = null,
                                rHandler = Pair(
                                    first = "네",
                                    second = { it ->
                                        finish()
                                    })
                            ).show(supportFragmentManager, "tag")
                        }
                        else ->
                        {
                            GeneralDialog(
                                act = parentActivity,
                                title = "오류",
                                msg = CodeUtil.returnCodeMsg(result?.code),
                                detailMsg = null,
                                rHandler = Pair(
                                    first = "네",
                                    second = { it ->

                                        finish()
                                    })
                            ).show(supportFragmentManager, "tag")
                        }
                    }
                }

            })


//        NetworkManager.getPrivateUserAPI(userRepo.getEmail(), userRepo.getApiPwd())
//            .requestAutoLogin(userRepo.getDeviceInfo(), userRepo.getJoinType(), null)
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribeOn(Schedulers.io())
//            .subscribe(
//                {
//
//
//                    if (it.code == ResponseCode.SUCCESS.CODE)
//                    {
//                        startActivity(Intent(parentActivity, MainView::class.java))
//                        finish()
//                    }
//                    else
//                    {
//                        GeneralDialog(
//                            parentActivity,
//                            "오류",
//                            CodeUtil.returnCodeMsg(it.code),
//                            null,
//                            Pair("네", { it ->
//                                it.dismiss()
//                                Log.d(TAG, "TESTESTSETSETS")
//                                userRepo.removeUserRepo()
//                                startActivity(Intent(parentActivity, IntroView::class.java))
//                                finish()
//                            })
//                        ).show(
//                            supportFragmentManager.beginTransaction(),
//                            "TAG"
//                        )
//                    }
//                },
//                {
//                    GeneralDialog(
//                        parentActivity,
//                        "오류",
//                        CodeUtil.returnCodeMsg(it.message!!),
//                        null,
//                        Pair("네", { it ->
//                            it.dismiss()
//                        })
//                    ).show(
//                        supportFragmentManager.beginTransaction(),
//                        "TAG"
//                    )
//                })
    }

    private fun requestPermission(permissionArray: Array<String>)
    {
        rxPermission.run {
            request(*permissionArray)
                .subscribe(
                    {
                        if (it)
                        {
                            splashVM.requestAfterActivity()
                        }
                        else
                        {
                            GeneralDialog(
                                act = parentActivity,
                                title = "알림",
                                msg = "쾌적한 앱 사용을 위해 권한을 허가해주세요.",
                                detailMsg = null,
                                rHandler = Pair(
                                    first = "네",
                                    second = { it ->
                                        it.dismiss()
                                        finish()
                                    })
                            ).show(supportFragmentManager, "tag")
                        }
                    },
                    {
                        Log.d(TAG, "Permission Error => $it")
                        splashVM.navigator.value = NavigatorConst.TO_INTRO
                    }
                )
        }

    }
}
