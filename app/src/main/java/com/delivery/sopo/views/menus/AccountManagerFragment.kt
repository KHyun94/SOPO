package com.delivery.sopo.views.menus

import android.view.View
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.databinding.FragmentAccountManagerBinding
import com.delivery.sopo.enums.OptionalTypeEnum
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.moveToActivity
import com.delivery.sopo.extensions.moveToActivityWithFinish
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.viewmodels.menus.AccountManagerViewModel
import com.delivery.sopo.viewmodels.menus.MenuMainFragment
import com.delivery.sopo.views.dialog.OptionalClickListener
import com.delivery.sopo.views.dialog.OptionalDialog
import com.delivery.sopo.views.login.LoginSelectView
import com.delivery.sopo.views.login.ResetPasswordView
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.registers.RegisterMainFragment
import com.google.android.material.snackbar.Snackbar
import org.koin.androidx.viewmodel.ext.android.viewModel

class AccountManagerFragment: BaseFragment<FragmentAccountManagerBinding, AccountManagerViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_account_manager
    override val vm: AccountManagerViewModel by viewModel()
    override val mainLayout: View by lazy { binding.linearMainAccountManager }

    private  val parentView: MainView by lazy { (requireActivity() as MainView) }

    override fun setBeforeBinding()
    {
        super.setBeforeBinding()

        useCommonBackPressListener(isUseCommon = true)

        onSOPOBackPressedListener = object: OnSOPOBackPressEvent(true)
        {
            override fun onBackPressed()
            {
                super.onBackPressed()
                TabCode.MY_MENU_MAIN.FRAGMENT = MenuFragment.newInstance()
                FragmentManager.move(requireActivity(), TabCode.MY_MENU_MAIN, MenuMainFragment.viewId)
            }
        }
    }

    override fun setObserve()
    {
        super.setObserve()

        activity ?: return
        parentView.currentPage.observe(this) {
            if(it != 2) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.navigator.observe(this) { navigator ->
            when(navigator)
            {
                NavigatorConst.TO_UPDATE_NICKNAME ->
                {
                    requireActivity().moveToActivity(UpdateNicknameView::class.java)
                }
                NavigatorConst.TO_RESET_PASSWORD ->
                {
                    requireActivity().moveToActivity(ResetPasswordView::class.java)
                }
                NavigatorConst.TO_LOGOUT ->
                {
                    /**
                     * TODO 전체 테이블 clear
                     */

                    val leftOptionalClickListener = object: OptionalClickListener {
                        override fun invoke(dialog: OptionalDialog)
                        {
                            vm.onLogout()
                            requireActivity().moveToActivity(LoginSelectView::class.java)
                            requireActivity().finish()
                            dialog.dismiss()
                        }
                    }

                    val rightOptionalClickListener = object: OptionalClickListener
                    {
                        override fun invoke(dialog: OptionalDialog)
                        {
                            dialog.dismiss()
                        }
                    }
                    val optionalDialog = OptionalDialog(optionalType = OptionalTypeEnum.RIGHT, titleIcon = 0, title = "로그아웃 하시겠어요?", subTitle = "", content = "이계정에 등록된 택배 정보는 로그아웃하셔도\n아이템 별로 90일까지 보관됩니다.",
                                                        leftHandler = Pair("로그아웃", second = leftOptionalClickListener),
                                                        rightHandler = Pair(first = "취소", second = rightOptionalClickListener))

                    optionalDialog.show(requireActivity().supportFragmentManager, "")
                }
                NavigatorConst.TO_SIGN_OUT ->
                {
                    requireActivity().moveToActivity(SignOutView::class.java)
                }
            }
        }
    }

    companion object
    {
        fun newInstance() = AccountManagerFragment()
    }
}