package com.delivery.sopo.views.inquiry

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.delivery.sopo.databinding.InquiryMainFrameBinding
import com.delivery.sopo.enums.FragmentTypeEnum
import com.delivery.sopo.interfaces.listener.OnMainBackPressListener
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.viewmodels.inquiry.InquiryMainViewModel
import com.delivery.sopo.views.main.MainView
import org.koin.androidx.viewmodel.ext.android.viewModel

class InquiryMainFrame : Fragment()
{
    lateinit var binding : InquiryMainFrameBinding
    val vm : InquiryMainViewModel by viewModel()
    private lateinit var parentView: MainView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        binding = InquiryMainFrameBinding.inflate(inflater, container, false)
        binding.vm = vm
        binding.lifecycleOwner = this
        viewId = binding.layoutMainFrame.id
        parentView = activity as MainView

        FragmentManager.move(this.requireActivity(), FragmentTypeEnum.INQUIRY, viewId)

        return binding.root
    }

//    override fun onResume()
//    {
//        super.onResume()
//        Log.d("!!!!", "InquiryMainFrame onResume() !!!!!!!!!!!!!!")
//        parentView.setOnBackPressListener(object : OnMainBackPressListener
//        {
//            override fun onBackPressed()
//            {
//                Log.d("!!!!!", "OnBackPressed InquiryView")
//                FragmentManager.remove(activity!!)
////                parentView.moveTaskToBack(true);                        // 태스크를 백그라운드로 이동
////                parentView.finishAndRemoveTask();                        // 액티비티 종료 + 태스크 리스트에서 지우기
////                android.os.Process.killProcess(android.os.Process.myPid());
//            }
//        })
//    }

    companion object{
        var viewId = 0
    }
}