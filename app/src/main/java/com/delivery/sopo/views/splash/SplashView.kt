package com.delivery.sopo.views.splash

import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.databinding.SplashViewBinding
import com.delivery.sopo.interfaces.listener.OnPermissionRequestListener
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.util.AlertUtil
import com.delivery.sopo.util.PermissionUtil
import com.delivery.sopo.viewmodels.splash.SplashViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.intro.IntroView
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.signup.RegisterNicknameView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel

class SplashView: BaseView<SplashViewBinding, SplashViewModel>()
{
    override val layoutRes: Int = R.layout.splash_view
    override val vm: SplashViewModel by viewModel()
    override val mainLayout by lazy { binding.constraintMainSplash }

    private val userLocalRepo: UserLocalRepository by inject()

    override fun setObserve()
    {
        super.setObserve()

        NotificationManagerCompat.getEnabledListenerPackages(applicationContext).any {
            it == packageName
        }

        val intent = Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")
        startActivity(intent)

//        Handler(Looper.getMainLooper()).postDelayed(Runnable { moveToActivity() }, 1500)
    }

    private fun moveToActivity()
    {
        vm.navigator.observe(this, Observer {
            when(it)
            {
                NavigatorConst.TO_PERMISSION ->
                {
                    PermissionUtil.requestPermission(this, object: OnPermissionRequestListener
                    {
                        override fun onPermissionGranted()
                        {
                            vm.requestLoginStatusForKeeping()
                        }

                        override fun onPermissionDenied()
                        {
                            // NOT PERMISSION GRANT
                            GeneralDialog(act = this@SplashView, title = getString(R.string.DIALOG_ALARM), msg = getString(R.string.DIALOG_PERMISSION_REQ_MSG), detailMsg = null, rHandler = Pair(first = getString(R.string.DIALOG_OK), second = { it ->
                                it.dismiss()
                                finish()
                            })).show(supportFragmentManager, "permission")
                        }
                    })
                }
                NavigatorConst.TO_INTRO ->
                {
                    startActivity(Intent(this, IntroView::class.java))
                    finish()
                }
                NavigatorConst.TO_MAIN ->
                {
                    goToMainOrNickname(userLocalRepo.getNickname())
                }
                NavigatorConst.TO_INIT ->
                {
                    CoroutineScope(Dispatchers.Main).launch {
                        AlertUtil.alertExpiredToken(this@SplashView, vm.errorMessage)
                    }
                }
            }
        })
    }

    // 정상적으로 로그인했을 때 닉네임의 여부에 따라 UpdateNicknameView or MainView로 이동
    private fun goToMainOrNickname(nickname: String)
    {
        val clz = if(nickname == "") RegisterNicknameView::class.java else MainView::class.java

        Intent(this@SplashView, clz).let { intent ->
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }
}
