package com.delivery.sopo.views.menus

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.delivery.sopo.databinding.FragmentNoticeBinding
import com.delivery.sopo.models.menu.NoticeItem
import com.delivery.sopo.viewmodels.menus.NoticeViewModel
import com.delivery.sopo.views.adapter.NoticeExpandableAdapter
import com.delivery.sopo.views.main.MainView
import kotlinx.android.synthetic.main.fragment_notice.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class NoticeFragment : Fragment(){

    private val vm: NoticeViewModel by viewModel()
    private lateinit var binding: FragmentNoticeBinding
    private lateinit var parentView: MainView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        parentView = activity as MainView
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        binding = FragmentNoticeBinding.inflate(inflater, container, false)
        viewBinding()
        setObserver()

        return binding.root
    }

    private fun viewBinding() {
        binding.vm = vm
        binding.lifecycleOwner = this
        binding.executePendingBindings() // 즉 바인딩
    }

    fun setObserver(){

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
        expandablelist_notice.setAdapter(noticeExpandableAdapter)
    }

    companion object{
        fun newInstance() = NoticeFragment()
    }
}