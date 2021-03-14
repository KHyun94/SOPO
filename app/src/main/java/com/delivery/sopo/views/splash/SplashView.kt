package com.delivery.sopo.views.splash

import android.content.Intent
import android.os.Handler
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.abstracts.BasicView
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.PermissionConst
import com.delivery.sopo.databinding.SplashViewBinding
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.models.LoginResult
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.LoginAPI
import com.delivery.sopo.repository.impl.OauthRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.OtherUtil
import com.delivery.sopo.util.PermissionUtil
import com.delivery.sopo.viewmodels.splash.SplashViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.dialog.PermissionDialog
import com.delivery.sopo.views.intro.IntroView
import com.delivery.sopo.views.main.MainView
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
    lateinit var permissionDialog: PermissionDialog

    init
    {
        parentActivity = this@SplashView
    }

    override fun bindView()
    {
        binding.vm = splashVm
        binding.lifecycleOwner = this
        binding.executePendingBindings()
    }

    override fun setObserver()
    {
        Handler().postDelayed(Runnable { moveToActivity() }, 500)
    }

    private fun moveToActivity()
    {
        binding.vm!!.navigator.observe(this, Observer {
            when (it)
            {
                NavigatorConst.TO_PERMISSION ->
                {
                    if (!PermissionUtil.isPermissionGranted(this, *PermissionConst.PERMISSION_ARRAY))
                    {
                        permissionDialog = PermissionDialog(act = parentActivity) { dialog ->

                            PermissionUtil.permissionCallback(
                                parentActivity, *PermissionConst.PERMISSION_ARRAY
                            ) { isGranted ->

                                if (!isGranted)
                                {
                                    // NOT PERMISSION GRANT
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
                                else
                                {
                                    binding.vm!!.requestAfterActivity()
                                }
                            }

                            dialog.dismiss()
                        }

                        permissionDialog.show(supportFragmentManager, "PermissionTag")
                    }
                    else
                    {
                        binding.vm!!.requestAfterActivity()
//                        binding.vm!!.navigator.value = NavigatorConst.TO_INTRO
                    }

                    // 권한 설정이 안되어있을 경우, 권한 허용 요청 다이얼로그 생성

                }
                NavigatorConst.TO_INTRO ->
                {
                    startActivity(Intent(parentActivity, IntroView::class.java))
                    finish()
                }
                NavigatorConst.TO_MAIN ->
                {
//                    requestAutoLogin()
                    startActivity(Intent(parentActivity, MainView::class.java))
                    finish()
                }
            }
        })
    }
}
