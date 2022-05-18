package com.delivery.sopo.presentation.views.inquiry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.delivery.sopo.databinding.InquiryMainFrameBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.presentation.viewmodels.inquiry.InquiryMainViewModel
import com.delivery.sopo.presentation.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class InquiryMainFragment : Fragment()
{
    lateinit var binding: InquiryMainFrameBinding
    val vm: InquiryMainViewModel by viewModel()
    private lateinit var parentView: MainView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = InquiryMainFrameBinding.inflate(inflater, container, false)
        binding.vm = vm
        binding.lifecycleOwner = this

        viewId = binding.layoutMainFrame.id
        parentView = activity as MainView

        TabCode.INQUIRY.FRAGMENT = InquiryFragment.newInstance(returnType = 0)
        FragmentManager.move(parentView, TabCode.INQUIRY, viewId)

        return binding.root
    }

    companion object
    {
        var viewId = 0
    }
}