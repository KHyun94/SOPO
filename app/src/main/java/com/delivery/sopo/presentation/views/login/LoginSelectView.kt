package com.delivery.sopo.presentation.views.login

import android.content.Intent
import android.view.View
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.consts.PermissionConst
import com.delivery.sopo.databinding.LoginSelectViewBinding
import com.delivery.sopo.extensions.launchActivityWithAllClear
import com.delivery.sopo.interfaces.listener.OnPermissionResponseCallback
import com.delivery.sopo.models.base.BaseView
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.presentation.viewmodels.login.LoginSelectViewModel
import com.delivery.sopo.presentation.views.dialog.GeneralDialog
import com.delivery.sopo.presentation.views.main.MainView
import com.delivery.sopo.presentation.views.signup.RegisterNicknameView
import com.delivery.sopo.presentation.views.signup.SignUpView
import com.delivery.sopo.thirdpartyapi.KakaoOath
import com.delivery.sopo.util.PermissionUtil
import com.delivery.sopo.util.SopoLog
import com.sothree.slidinguppanel.SlidingUpPanelLayout
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginSelectView : BaseView<LoginSelectViewBinding, LoginSelectViewModel>()
{
    override val layoutRes: Int=R.layout.login_select_view
    override val vm : LoginSelectViewModel by viewModel()
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
                    startActivity(Intent(this, LoginView::class.java))
                }
                NavigatorConst.TO_SIGN_UP ->
                {
                    startActivity(Intent(this, SignUpView::class.java))
                }
                NavigatorConst.Screen.MAIN ->
                {
                    Intent(this, MainView::class.java).launchActivityWithAllClear(this@LoginSelectView)
                }
                NavigatorConst.Screen.UPDATE_NICKNAME ->
                {
                    Intent(this, RegisterNicknameView::class.java).launchActivityWithAllClear(this@LoginSelectView)
                }
                NavigatorConst.TO_KAKAO_LOGIN ->
                {
                    vm.requestKakaoLogin(KakaoOath(this))
                }
            }
        }
    }
}