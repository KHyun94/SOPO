package com.delivery.sopo.views.registers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import com.delivery.sopo.R
import com.delivery.sopo.databinding.RegisterMainFrameBinding
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import java.lang.Exception

class RegisterMainFrame : Fragment()
{
    private lateinit var binding : RegisterMainFrameBinding

    override fun onCreateView(
        inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?
    ) : View
    {
        binding = bindView<RegisterMainFrameBinding>(inflater, R.layout.register_main_frame, container)
        viewId = binding.frameRegister.id

        try{
            // Tab1로 이동
            FragmentManager.move(requireActivity(), TabCode.REGISTER_STEP1, viewId)
        }catch (e: Exception)
        {
            SopoLog.e(msg =  "아 시발 에러 ${e.message}", e = e)
        }


        return binding.root
    }

    fun <T : ViewDataBinding> bindView(inflater : LayoutInflater, @LayoutRes resId : Int, container : ViewGroup?) : T
    {
        val binding : T = DataBindingUtil.inflate(inflater, resId, container, false)
        binding.lifecycleOwner = this
        return binding
    }

    companion object
    {

        // Register Tab의 MainFrameLayout id
        var viewId : Int = 0
    }
}