package com.delivery.sopo.views

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.delivery.sopo.GeneralDialog
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.PermissionConst
import com.delivery.sopo.databinding.SplashViewBinding
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.interfaces.BasicView
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.UserRepo
import com.delivery.sopo.util.fun_util.CodeUtil
import com.delivery.sopo.util.ui_util.PermissionDialog
import com.delivery.sopo.viewmodels.SplashViewModel
import com.tbruyelle.rxpermissions2.RxPermissions
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashView : BasicView<SplashViewBinding>(
    layoutRes = R.layout.splash_view
)
{
    /*
    todo 스플래시 화면 1~3
    1 권한 검사(외부 저장소 읽기/쓰기) - RxPermission을 사용
    2 내부 DB(Shared Preference)에 저장되어있는 개인 API 계정과 비밀번호가 있는지 체크
        2.1 있다면 디바이스 정보와 로그인 타입을 서버로 전송
        2.2 없다면 인트로 화면으로 이동
    3 MVVM 패턴 적용
    */

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
        NetworkManager.getPrivateUserAPI(userRepo.getEmail(), userRepo.getApiPwd())
            .requestAutoLogin(userRepo.getDeviceInfo(), userRepo.getJoinType())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeOn(Schedulers.io())
            .subscribe(
                {
                    if (it.code == ResponseCode.SUCCESS.CODE)
                    {
                        startActivity(Intent(parentActivity, MainView::class.java))
                        finish()
                    }
                    else
                    {
                        GeneralDialog(
                            parentActivity,
                            "오류",
                            CodeUtil.returnCodeMsg(it.code),
                            null,
                            Pair("네", { it ->
                                it.dismiss()
                                Log.d(TAG,"TESTESTSETSETS")
                                userRepo.removeUserRepo()
                                startActivity(Intent(parentActivity, IntroView::class.java))
                                finish()
                            })
                        ).show(
                            supportFragmentManager.beginTransaction(),
                            "TAG"
                        )
                    }
                },
                {
                    GeneralDialog(
                        parentActivity,
                        "오류",
                        CodeUtil.returnCodeMsg(it.message!!),
                        null,
                        Pair("네", { it ->
                            it.dismiss()
                        })
                    ).show(
                        supportFragmentManager.beginTransaction(),
                        "TAG"
                    )
                })
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
