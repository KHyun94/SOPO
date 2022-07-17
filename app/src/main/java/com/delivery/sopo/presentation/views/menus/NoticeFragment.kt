package com.delivery.sopo.presentation.views.menus

import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebViewClient
import com.delivery.sopo.R
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.databinding.FragmentNoticeBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.interfaces.listener.OnSOPOBackPressEvent
import com.delivery.sopo.models.base.BaseFragment
import com.delivery.sopo.models.menu.NoticeItem
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.presentation.viewmodels.menus.MenuMainFragment
import com.delivery.sopo.presentation.viewmodels.menus.NoticeViewModel
import com.delivery.sopo.presentation.views.adapter.NoticeExpandableAdapter
import com.delivery.sopo.presentation.views.main.MainView
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
        parentView.getCurrentPage().observe(this) {
            if(it != 2) return@observe
            requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
        }

        vm.navigator.observe(this) { nav ->
            when(nav)
            {
                NavigatorConst.Event.BACK ->
                {
                    FragmentManager.refreshMove(parentView, TabCode.MY_MENU_MAIN.apply {
                        FRAGMENT = MenuFragment.newInstance()
                    }, MenuMainFragment.viewId)
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        binding.webViewNotice.webViewClient = WebViewClient()
        val webSettings = binding.webViewNotice.settings
        webSettings.javaScriptEnabled = true // 웹페이지 자바스클비트 허용 여부
        webSettings.setSupportMultipleWindows(false) // 새창 띄우기 허용 여부
        webSettings.javaScriptCanOpenWindowsAutomatically = false // 자바스크립트 새창 띄우기(멀티뷰) 허용 여부
        webSettings.loadWithOverviewMode = true // 메타태그 허용 여부
        webSettings.useWideViewPort = true // 화면 사이즈 맞추기 허용 여부
        webSettings.setSupportZoom(false) // 화면 줌 허용 여부
        webSettings.builtInZoomControls = false // 화면 확대 축소 허용 여부
        webSettings.layoutAlgorithm = WebSettings.LayoutAlgorithm.SINGLE_COLUMN // 컨텐츠 사이즈 맞추기
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE // 브라우저 캐시 허용 여부
        webSettings.domStorageEnabled = true // 로컬저장소 허용 여부

        binding.webViewNotice.loadUrl("https://harsh-sing-e14.notion.site/SOPO-7b3a0de113714d7681fc2b77d2a91b8f")



       /* val data = mutableListOf<NoticeItem>()

        val notice1Content = mutableListOf<String>()
        notice1Content.add("안녕하세요. SOPO 사용자 여러분.\n SOPO 앱 버전 1.1.0으로 업데이트하면서 변화된 점에 대하여 공지드립니다.\n\n [업데이트]\n1. UI 개선\n2. 택배 예약 기능 추가.")
        val notice1 = NoticeItem("SOPO 1.1.0 버전 업데이트 안내", "2020/08/19", notice1Content)
        val notice2Content = mutableListOf<String>()
        notice2Content.add("안녕하세요. SOPO 사용자 여러분.\n SOPO 앱 개발진을 대표하여 공지사항 전달드립니다.")
        val notice2 = NoticeItem("서비스 일시 중단 안내", "2020/08/25", notice2Content)

        data.add(notice1)
        data.add(notice2)

        val noticeExpandableAdapter = NoticeExpandableAdapter(requireContext() , data)
        binding.expandablelistNotice.setAdapter(noticeExpandableAdapter)*/
    }

    companion object{
        fun newInstance() = NoticeFragment()
    }
}