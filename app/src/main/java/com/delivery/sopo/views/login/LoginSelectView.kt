package com.delivery.sopo.views.login

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.PermissionConst
import com.delivery.sopo.databinding.LoginSelectViewBinding
import com.delivery.sopo.extensions.launchActivityWithAllClear
import com.delivery.sopo.extensions.moveToActivityWithFinish
import com.delivery.sopo.interfaces.listener.OnPermissionResponseCallback
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.util.PermissionUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.login.LoginSelectViewModel
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.signup.RegisterNicknameView
import com.delivery.sopo.views.signup.SignUpView
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.util.exception.KakaoException
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginSelectView : BaseView<LoginSelectViewBinding, LoginSelectViewModel>()
{
    override val layoutRes: Int=R.layout.login_select_view
    override val vm : LoginSelectViewModel by viewModel()
    override val mainLayout: View by lazy { binding.constraintMainLoginSelect }

    private var sessionCallback : ISessionCallback? = null

    private val onPermissionResponseCallback = object: OnPermissionResponseCallback
    {
        override fun onPermissionGranted()
        {

        }

        override fun onPermissionDenied()
        {
            // NOT PERMISSION GRANT
            GeneralDialog(act = this@LoginSelectView, title = getString(R.string.DIALOG_ALARM), msg = getString(R.string.DIALOG_PERMISSION_REQ_MSG), detailMsg = null, rHandler = Pair(first = getString(R.string.DIALOG_OK), second = { dialog ->
                dialog.dismiss()
                finish()
            })).show(supportFragmentManager, "permission")
        }
    }

    override fun onAfterBinding()
    {
        super.onAfterBinding()

        if(!PermissionUtil.isPermissionGranted(this, *PermissionConst.PERMISSION_ARRAY)){
            binding.slideMainLoginSelect.panelState = SlidingUpPanelLayout.PanelState.EXPANDED
        }

        binding.tvPermissionCheck.setOnClickListener {
            binding.slideMainLoginSelect.panelState = SlidingUpPanelLayout.PanelState.COLLAPSED
            PermissionUtil.requestPermission(this, onPermissionResponseCallback)
        }

    }

    override fun setObserve()
    {
        super.setObserve()

        vm.navigator.observe(this, Observer { navigator ->

            SopoLog.d("navigator:$navigator")

            when (navigator)
            {
                NavigatorConst.TO_LOGIN ->
                {
                    startActivity(Intent(this, LoginView::class.java))
                }
                NavigatorConst.TO_SIGN_UP ->
                {
                    startActivity(Intent(this, SignUpView::class.java))
                }
                NavigatorConst.TO_MAIN ->
                {
                    Intent(this, MainView::class.java).launchActivityWithAllClear(this@LoginSelectView)
                }
                NavigatorConst.TO_UPDATE_NICKNAME ->
                {
                    Intent(this, RegisterNicknameView::class.java).launchActivityWithAllClear(this@LoginSelectView)
                }
                NavigatorConst.TO_KAKAO_LOGIN ->
                {
                    SopoLog.d("카카오 로그인 시작")
                    binding.btnKakaoLogin.performClick()

                    if (Session.getCurrentSession() != null) Session.getCurrentSession().removeCallback(sessionCallback)

                    sessionCallback = object : ISessionCallback
                    {
                        override fun onSessionOpened()
                        {
                            SopoLog.d("카카오 로그인 세젼 오픈")
                            vm.requestKakaoLogin()
                        }

                        override fun onSessionOpenFailed(exception : KakaoException)
                        {
                            SopoLog.e( msg = "카카오 세션 에러: ${exception}", e = exception)
                        }
                    }
                    Session.getCurrentSession().addCallback(sessionCallback)
                }
            }
        })
    }

    // TODO KAKAO Session Return 처리
    override fun onActivityResult(requestCode : Int, resultCode : Int, data : Intent?)
    {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data))
        {
            return
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy()
    {
        super.onDestroy()
        sessionCallback?.run { Session.getCurrentSession().removeCallback(this) }
    }




}