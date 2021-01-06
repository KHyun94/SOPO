package com.delivery.sopo.views.splash

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.PermissionConst
import com.delivery.sopo.databinding.SplashViewBinding
import com.delivery.sopo.enums.ResponseCodeEnum
import com.delivery.sopo.models.LoginResult
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.LoginAPI
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.splash.SplashViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.dialog.PermissionDialog
import com.delivery.sopo.views.intro.IntroView
import com.delivery.sopo.views.main.MainView
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.android.synthetic.main.splash_view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SplashView : BasicView<SplashViewBinding>(layoutRes = R.layout.splash_view)
{
    private val splashVm: SplashViewModel by viewModel()

    private val userRepoImpl: UserRepoImpl by inject()

    lateinit var rxPermission: RxPermissions
    lateinit var permissionDialog: PermissionDialog

    init
    {
        TAG += this.javaClass.simpleName
        parentActivity = this@SplashView
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        rxPermission = RxPermissions.getInstance(applicationContext)


    }

    override fun bindView()
    {
        binding.vm = splashVm
        binding.lifecycleOwner = this
        binding.executePendingBindings()
    }

    override fun setObserver()
    {
        Handler().postDelayed(Runnable { moveToActivity() }, 2500)
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
        binding.vm!!.navigator.observe(this, Observer {
            when (it)
            {
                NavigatorConst.TO_PERMISSION ->
                {
                    // 권한 설정이 안되어있을 경우, 권한 허용 요청 다이얼로그 생성
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
                        binding.vm!!.requestAfterActivity()
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

    private fun requestAutoLogin()
    {
        NetworkManager.initPrivateApi(userRepoImpl.getEmail(), userRepoImpl.getApiPwd())

        val firebaseUser = SOPOApp.auth.currentUser

        NetworkManager.privateRetro.create(LoginAPI::class.java)
            .requestAutoLogin(
                deviceInfo = OtherUtil.getDeviceID(SOPOApp.INSTANCE),
                joinType = userRepoImpl.getJoinType(),
                uid = firebaseUser?.uid!!,
                kakaoUserId = userRepoImpl.getSNSUId()
            ).enqueue(object : Callback<APIResult<LoginResult?>>
            {
                override fun onFailure(call: Call<APIResult<LoginResult?>>, t: Throwable)
                {
//                    TODO("Not yet implemented")
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
                        ResponseCodeEnum.SUCCESS.HTTP_STATUS ->
                        {
                            startActivity(Intent(parentActivity, MainView::class.java))
                            finish()
                        }
                        ResponseCodeEnum.INVALID_USER.HTTP_STATUS ->
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
    }

    // 권한 요청
    private fun requestPermission(permissionArray: Array<String>)
    {
        rxPermission.run {
            request(*permissionArray)
                .subscribe(
                    {
                        if (it)
                        {
                            // 권한 O
                            binding.vm!!.requestAfterActivity()
                        }
                        else
                        {
                            // 권한 X
                            GeneralDialog(
                                act = parentActivity,
                                title = getString(R.string.DIALOG_ALARM),
                                msg = getString(R.string.DIALOG_PERMISSION_REQ_MSG),
                                detailMsg = null,
                                rHandler = Pair(
                                    first = getString(R.string.DIALOG_OK),
                                    second = { it ->
                                        it.dismiss()
                                        finish()
                                    })
                            ).show(supportFragmentManager, "permission")
                        }
                    },
                    {
                        SopoLog.e(tag = TAG, msg = "Permission Error => $it", e = it)
                        binding.vm!!.navigator.value = NavigatorConst.TO_INTRO
                    }
                )
        }

    }
}
