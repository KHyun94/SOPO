package com.delivery.sopo.presentation.views.inquiry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.delivery.sopo.databinding.InquiryMainFrameBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.presentation.viewmodels.inquiry.InquiryMainViewModel
import com.delivery.sopo.presentation.views.main.MainActivity
import dagger.hilt.android.AndroidEntryPoint
import org.koin.androidx.viewmodel.ext.android.viewModel

@AndroidEntryPoint
class InquiryMainFragment : Fragment()
{
    lateinit var binding: InquiryMainFrameBinding
    val vm: InquiryMainViewModel by viewModels()
    private lateinit var parentActivity: MainActivity

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        binding = InquiryMainFrameBinding.inflate(inflater, container, false)
        binding.vm = vm
        binding.lifecycleOwner = this

        viewId = binding.layoutMainFrame.id
        parentActivity = activity as MainActivity

        TabCode.INQUIRY.FRAGMENT = InquiryFragment.newInstance(returnType = 0)
        FragmentManager.move(parentActivity, TabCode.INQUIRY, viewId)

        return binding.root
    }

    companion object
    {
        var viewId = 0
    }
}