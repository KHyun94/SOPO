package com.delivery.sopo.presentation.splash

import android.content.Intent
import androidx.activity.viewModels
import com.delivery.sopo.R
import com.delivery.sopo.databinding.SplashViewBinding
import com.delivery.sopo.extensions.moveActivity
import com.delivery.sopo.interfaces.listener.OnPermissionResponseCallback
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.presentation.views.dialog.GeneralDialog
import com.delivery.sopo.presentation.views.intro.IntroView
import com.delivery.sopo.presentation.views.main.MainActivity
import com.delivery.sopo.presentation.views.signup.RegisterNicknameView
import com.delivery.sopo.util.PermissionUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.WindowUtil
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashView: BaseView<SplashViewBinding, SplashViewModel>()
{
    override val layoutRes: Int = R.layout.splash_view
    override val vm: SplashViewModel by viewModels()
    override val mainLayout by lazy { binding.constraintMainSplash }

    private val onPermissionResponseCallback = object: OnPermissionResponseCallback {
        override fun onPermissionGranted()
        {
            vm.requestUserInfo()
        }

        override fun onPermissionDenied()
        {
            GeneralDialog(act = this@SplashView,
                          title = getString(R.string.DIALOG_ALARM),
                          msg = getString(R.string.DIALOG_PERMISSION_REQ_MSG),
                          detailMsg = null,
                          rHandler = Pair(first = getString(R.string.DIALOG_OK), second = { dialog ->
                              dialog.dismiss()
                              finish()
                          })).show(supportFragmentManager, "permission")
        }
    }

    override fun onBeforeBinding()
    {
        super.onBeforeBinding()

        WindowUtil.setWindowStatusBarColor(this, R.color.COLOR_MAIN_700)
    }

    override fun setObserve()
    {
        super.setObserve()

        vm.navigator.observe(this) {

            SopoLog.d("navigator [$it]")

            when(it)
            {
                NavigatorConst.TO_INTRO ->
                {
                    moveActivity(IntroView::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK){
                        finish()
                    }
                }
                NavigatorConst.TO_PERMISSION ->
                {
                    PermissionUtil.requestPermission(this, onPermissionResponseCallback)
                }
                NavigatorConst.Screen.UPDATE_NICKNAME ->
                {
                    moveActivity(RegisterNicknameView::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK){
                        finish()
                    }
                }
                NavigatorConst.Screen.MAIN ->
                {
                    moveActivity(MainActivity::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK){
                        finish()
                    }
                }
            }
        }

    }
}
