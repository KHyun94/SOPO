package com.delivery.sopo.presentation.login

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import com.delivery.sopo.R
import com.delivery.sopo.consts.PermissionConst
import com.delivery.sopo.databinding.LoginSelectViewBinding
import com.delivery.sopo.extensions.moveActivity
import com.delivery.sopo.interfaces.listener.OnPermissionResponseCallback
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.presentation.views.dialog.GeneralDialog
import com.delivery.sopo.presentation.views.main.MainActivity
import com.delivery.sopo.presentation.views.signup.RegisterNicknameView
import com.delivery.sopo.presentation.views.signup.SignUpView
import com.delivery.sopo.presentation.login.oath.KakaoOath
import com.delivery.sopo.util.PermissionUtil
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginSelectView : BaseView<LoginSelectViewBinding, LoginSelectViewModel>()
{
    override val layoutRes: Int=R.layout.login_select_view
    override val vm : LoginSelectViewModel by viewModels()
    override val mainLayout: View by lazy { binding.constraintMainLoginSelect }

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

        vm.navigator.observe(this) { navigator ->

            when(navigator)
            {
                NavigatorConst.TO_LOGIN ->
                {
                    moveActivity(LoginView::class.java)
                }
                NavigatorConst.TO_SIGN_UP ->
                {
                    moveActivity(SignUpView::class.java)
                }
                NavigatorConst.Screen.MAIN ->
                {
                    moveActivity(MainActivity::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK) {
                        finish()
                    }
                }
                NavigatorConst.Screen.UPDATE_NICKNAME ->
                {
                    moveActivity(RegisterNicknameView::class.java, Intent.FLAG_ACTIVITY_CLEAR_TASK) {
                        finish()
                    }
                }
                NavigatorConst.TO_KAKAO_LOGIN ->
                {
                    vm.requestKakaoLogin(KakaoOath(this))
                }
            }
        }
    }
}