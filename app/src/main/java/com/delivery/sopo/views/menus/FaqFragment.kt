package com.delivery.sopo.views.menus

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.delivery.sopo.databinding.FragmentFaqBinding
import com.delivery.sopo.models.menu.FaqItem
import com.delivery.sopo.viewmodels.menus.FaqViewModel
import com.delivery.sopo.views.adapter.FaqExpandableAdapter
import com.delivery.sopo.views.dialog.OtherFaqDialog
import com.delivery.sopo.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class FaqFragment: Fragment(){

    private val faqVM: FaqViewModel by viewModel()
    private lateinit var binding:FragmentFaqBinding
    private lateinit var parentView: MainView

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {

        parentView = activity as MainView

        binding = FragmentFaqBinding.inflate(inflater, container, false)
        viewBinding()
        setObserver()
        setListener()

        return binding.root
    }

    private fun viewBinding() {
        binding.vm = faqVM
        binding.lifecycleOwner = this
        binding.executePendingBindings() // 즉 바인딩
    }

    fun setObserver(){

    }

    private fun setListener(){
        binding.tvComment.setOnClickListener {
            OtherFaqDialog().show(requireActivity().supportFragmentManager, "OtherFaqDialog")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

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
    companion object{
        fun newInstance() = AppInfoFragment()
    }
}