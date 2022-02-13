package com.delivery.sopo.views.menus

import android.os.Bundle
import android.view.View
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentFaqBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.menu.FaqItem
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.menus.FaqViewModel
import com.delivery.sopo.viewmodels.menus.MenuMainFragment
import com.delivery.sopo.views.adapter.FaqExpandableAdapter
import com.delivery.sopo.views.dialog.OtherFaqDialog
import com.delivery.sopo.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class FaqFragment: BaseFragment<FragmentFaqBinding, FaqViewModel>(){

    override val vm: FaqViewModel by viewModel()
    override val layoutRes: Int = R.layout.fragment_faq
    override val mainLayout: View by lazy{ binding.linearMainFaq }
    private val parentView: MainView by lazy { activity as MainView }

    override fun setBeforeBinding()
    {
        super.setBeforeBinding()

        useCommonBackPressListener(isUseCommon = true)

        onSOPOBackPressedListener = object: OnSOPOBackPressEvent(true)
        {
            override fun onBackPressed()
            {
                super.onBackPressed()
                SopoLog.d("TEST Back")
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
            SopoLog.d("TEST Back re")
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }
    }

    override fun setAfterBinding()
    {
        super.setAfterBinding()

        setListener()

        val data = mutableListOf<FaqItem>()

        val faq1Content = mutableListOf<String>()
        faq1Content.add("반짝 반짝 작별 아름답게 비치네 서쪽 하늘에서도 동쪽하늘 에서도 반짝반짝 작은")
        val faq1 = FaqItem("FAQ 1",  faq1Content)
        val faq2Content = mutableListOf<String>()
        faq2Content.add("동해물과 백두산이 마르고 닳도록 하느님이 보우하사, 우리나라 만세~")
        val faq2 = FaqItem("FAQ 2",  faq2Content)

        data.add(faq1)
        data.add(faq2)

        val faqExpandableAdapter = FaqExpandableAdapter(requireContext() , data)
        binding.expandFaq.setAdapter(faqExpandableAdapter)
    }
    private fun setListener(){
        binding.tvComment.setOnClickListener {
            OtherFaqDialog().show(requireActivity().supportFragmentManager, "OtherFaqDialog")
        }
    }

    companion object{
        fun newInstance() = AppInfoFragment()
    }
}