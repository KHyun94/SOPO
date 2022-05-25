package com.delivery.sopo.presentation.views.splash

import android.content.Intent
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.SplashViewBinding
import com.delivery.sopo.extensions.moveToActivityWithFinish
import com.delivery.sopo.interfaces.listener.OnPermissionResponseCallback
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.util.PermissionUtil
import com.delivery.sopo.util.WindowUtil
import com.delivery.sopo.presentation.viewmodels.splash.SplashViewModel
import com.delivery.sopo.presentation.views.dialog.GeneralDialog
import com.delivery.sopo.presentation.views.intro.IntroView
import com.delivery.sopo.presentation.views.main.MainView
import com.delivery.sopo.presentation.views.signup.RegisterNicknameView
import com.delivery.sopo.util.SopoLog
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashView: BaseView<SplashViewBinding, SplashViewModel>()
{
    override val layoutRes: Int = R.layout.splash_view
    override val vm: SplashViewModel by viewModel()
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
                    moveToActivityWithFinish(IntroView::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    finish()
                }
                NavigatorConst.TO_PERMISSION ->
                {
                    PermissionUtil.requestPermission(this, onPermissionResponseCallback)
                }
                NavigatorConst.TO_UPDATE_NICKNAME ->
                {
                    moveToActivityWithFinish(RegisterNicknameView::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    finish()
                }
                NavigatorConst.TO_MAIN ->
                {
                    moveToActivityWithFinish(MainView::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    finish()
                }
                NavigatorConst.DUPLICATE_LOGIN ->
                {

                }
            }
        }

    }
}
