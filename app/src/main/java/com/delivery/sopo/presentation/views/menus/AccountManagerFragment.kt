package com.delivery.sopo.presentation.views.menus

import android.view.View
import androidx.fragment.app.viewModels
import com.delivery.sopo.R
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.databinding.FragmentAccountManagerBinding
import com.delivery.sopo.enums.DialogType
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.extensions.makeGone
import com.delivery.sopo.extensions.makeVisible
import com.delivery.sopo.extensions.moveActivity
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.presentation.viewmodels.menus.AccountManagerViewModel
import com.delivery.sopo.presentation.viewmodels.menus.MenuMainFragment
import com.delivery.sopo.presentation.views.dialog.CommonDialog
import com.delivery.sopo.presentation.views.login.ResetPasswordView
import com.delivery.sopo.presentation.views.main.MainActivity
import com.delivery.sopo.util.SopoLog
import dagger.hilt.android.AndroidEntryPoint
import org.koin.androidx.viewmodel.ext.android.viewModel

@AndroidEntryPoint
class AccountManagerFragment: BaseFragment<FragmentAccountManagerBinding, AccountManagerViewModel>()
{
    override val layoutRes: Int = R.layout.fragment_account_manager
    override val vm: AccountManagerViewModel by viewModels()
    override val mainLayout: View by lazy { binding.linearMainAccountManager }

    private  val parentActivity: MainActivity by lazy { (requireActivity() as MainActivity) }

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
        parentActivity.getCurrentPage().observe(this) {
            if(it != 2) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.nickname.observe(this) {
            SopoLog.d("nickname $it")
        }

        vm.navigator.observe(this) { navigator ->
            when(navigator)
            {
                NavigatorConst.Event.BACK ->
                {
                    TabCode.MY_MENU_MAIN.FRAGMENT = MenuFragment.newInstance()
                    FragmentManager.move(requireActivity(), TabCode.MY_MENU_MAIN, MenuMainFragment.viewId)
                }
                NavigatorConst.Screen.UPDATE_NICKNAME ->
                {
                    showKeyboard(binding.includeBottomInputLayout.etInputText)
                    binding.includeBottomInputLayout.root.makeVisible()
                    binding.includeBottomInputLayout.onClickListener = View.OnClickListener {
                        val nickname: String = binding.includeBottomInputLayout.etInputText.text.toString()
                        vm.onUpdateNickname(nickname)
                    }
                }
                NavigatorConst.Screen.RESET_PASSWORD ->
                {
                    requireActivity().moveActivity(ResetPasswordView::class.java)
                }
                NavigatorConst.TO_LOGOUT ->
                {
                    val optionalDialog = CommonDialog(dialogType = DialogType.FocusRightButton("로그아웃", "취소"), title = "로그아웃 하시겠어요?", content = "이계정에 등록된 택배 정보는 로그아웃하셔도\n아이템 별로 90일까지 보관됩니다.",
                                                      onLeftClickListener = { dialog ->
                                                          vm.onLogout()
                                                          exit()
                                                          dialog.dismiss()
                                                      },
                                                      onRightClickListener = { dialog ->
                                                          dialog.dismiss()
                                                      })

                    optionalDialog.show(requireActivity().supportFragmentManager, "")
                }
                NavigatorConst.TO_SIGN_OUT ->
                {
                    requireActivity().moveActivity(SignOutView::class.java)
                }
                "UPDATE_COMPLETED" ->
                {
                    binding.includeBottomInputLayout.etInputText.setText("")
                    hideKeyboard()
                }
            }
        }
    }

    override fun onShowKeyboard()
    {
        super.onShowKeyboard()
    }

    override fun onHideKeyboard()
    {
        super.onHideKeyboard()

        binding.includeBottomInputLayout.root.makeGone()
    }

    companion object
    {
        fun newInstance() = AccountManagerFragment()
    }
}