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
import com.delivery.sopo.views.main.MainView

class RegisterMainFragment : Fragment()
{
    private lateinit var binding : RegisterMainFrameBinding
    private lateinit var parentView: MainView

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        parentView = activity as MainView
    }

    override fun onCreateView(
        inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?
    ) : View
    {
        binding = bindView<RegisterMainFrameBinding>(inflater, R.layout.register_main_frame, container)
        layoutId = binding.layoutRegisterMain.id

        FragmentManager.move(requireActivity(), TabCode.REGISTER_STEP1, layoutId)

        return binding.root
    }

    fun <T : ViewDataBinding> bindView(inflater : LayoutInflater, @LayoutRes layoutId : Int, container : ViewGroup?) : T
    {
        return DataBindingUtil.inflate<T>(inflater, layoutId, container, false).apply{
            lifecycleOwner = this@RegisterMainFragment
            executePendingBindings()
        }
    }

    companion object
    {
        var layoutId : Int = 0

        const val WAYBILL_NO = "WAYBILL_NO"
        const val REGISTER_INFO = "REGISTER_INFO"
        // 다른 프래그먼트에서 돌아왔을 때 분기 처리
        // 0: Default 1: Success To Register
        const val RETURN_TYPE = "RETURN_TYPE"
        const val REGISTER_REVISE = "REGISTER_REVISE"
        const val REGISTER_INIT = "REGISTER_INIT"
        const val REGISTER_CONFIRM = "REGISTER_CONFIRM"
    }
}