package com.delivery.sopo.presentation.register.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentRegisterParcelBinding
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.presentation.models.enums.RegisterNavigation
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class RegisterParcelFragment : Fragment()
{
    private lateinit var binding : FragmentRegisterParcelBinding

    override fun onCreateView(inflater : LayoutInflater, container : ViewGroup?, savedInstanceState : Bundle?) : View
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register_parcel, container, false)
        binding.lifecycleOwner = this@RegisterParcelFragment
        binding.executePendingBindings()

        viewId = binding.layoutRegisterMain.id

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        val fm = childFragmentManager.beginTransaction()
        fm.add(viewId, InputParcelFragment.newInstance(RegisterNavigation.Init), "inputParcel")
        fm.commit()
    }

    companion object
    {
        var viewId : Int = 0

        const val WAYBILL_NO = "WAYBILL_NO"
        const val REGISTER_INFO = "REGISTER_INFO"
        const val BEFORE_STEP = "BEFORE_STEP"
        // 다른 프래그먼트에서 돌아왔을 때 분기 처리
        // 0: Default 1: Success To Register
        const val RETURN_TYPE = "RETURN_TYPE"
    }
}