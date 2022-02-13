package com.delivery.sopo.views.menus

import android.os.Bundle
import android.view.View
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentNoticeBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.menu.NoticeItem
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.viewmodels.menus.MenuMainFragment
import com.delivery.sopo.viewmodels.menus.NoticeViewModel
import com.delivery.sopo.views.adapter.NoticeExpandableAdapter
import com.delivery.sopo.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class NoticeFragment : BaseFragment<FragmentNoticeBinding, NoticeViewModel>(){

    override val vm: NoticeViewModel by viewModel()
    override val layoutRes: Int = R.layout.fragment_notice
    override val mainLayout: View by lazy{ binding.constraintMainNotice }
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val data = mutableListOf<NoticeItem>()

        val notice1Content = mutableListOf<String>()
        notice1Content.add("안녕하세요. SOPO 사용자 여러분.\n SOPO 앱 버전 1.1.0으로 업데이트하면서 변화된 점에 대하여 공지드립니다.\n\n [업데이트]\n1. UI 개선\n2. 택배 예약 기능 추가.")
        val notice1 = NoticeItem("SOPO 1.1.0 버전 업데이트 안내", "2020/08/19", notice1Content)
        val notice2Content = mutableListOf<String>()
        notice2Content.add("안녕하세요. SOPO 사용자 여러분.\n SOPO 앱 개발진을 대표하여 공지사항 전달드립니다.")
        val notice2 = NoticeItem("서비스 일시 중단 안내", "2020/08/25", notice2Content)

        data.add(notice1)
        data.add(notice2)

        val noticeExpandableAdapter = NoticeExpandableAdapter(requireContext() , data)
        binding.expandablelistNotice.setAdapter(noticeExpandableAdapter)
    }

    companion object{
        fun newInstance() = NoticeFragment()
    }
}