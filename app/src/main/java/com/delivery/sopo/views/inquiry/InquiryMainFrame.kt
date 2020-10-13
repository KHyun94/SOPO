package com.delivery.sopo.views.inquiry

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delivery.sopo.databinding.InquiryMainFrameBinding
import com.delivery.sopo.enums.FragmentTypeEnum
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.viewmodels.inquiry.InquiryMainViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel

class InquiryMainFrame : Fragment()
{
    lateinit var binding : InquiryMainFrameBinding
    val vm : InquiryMainViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        binding = InquiryMainFrameBinding.inflate(inflater, container, false)
        binding.vm = vm
        binding.lifecycleOwner = this

        viewId = binding.layoutMainFrame.id

        FragmentManager.move(activity!!, FragmentTypeEnum.INQUIRY, viewId)

        return binding.root
    }

    companion object{
        var viewId = 0
    }
}