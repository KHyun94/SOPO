package com.delivery.sopo.views.registers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Observer
import com.delivery.sopo.R
import com.delivery.sopo.databinding.RegisterTrackNumBinding
import com.delivery.sopo.enums.FragmentType
import com.delivery.sopo.viewmodels.registesrs.RegisterViewModel
import com.delivery.sopo.views.MainView
import kotlinx.android.synthetic.main.main_view.*
import org.koin.androidx.viewmodel.ext.android.viewModel

class RegisterTrackNum : Fragment()
{
    private lateinit var binding: RegisterTrackNumBinding
    private val registerVm: RegisterViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.register_track_num, container, false)
        binding.vm = registerVm
        binding.lifecycleOwner = this

        setObserve()

        return binding.root
    }

    fun setObserve()
    {
        binding.vm?.moveFragment?.observe(this, Observer {

            when (it)
            {
                FragmentType.REGISTER_STEP1.NAME -> {
                    (activity as MainView).binding.vm?.current1stTabName?.value = FragmentType.REGISTER_STEP1.NAME
//                    com.delivery.sopo.util.ui_util.FragmentManager.move(activity as MainView, FragmentType.REGISTER_STEP1, (activity as MainView).frame_container.id)
                }

                FragmentType.REGISTER_STEP2.NAME -> {
                    (activity as MainView).binding.vm?.current1stTabName?.value = FragmentType.REGISTER_STEP2.NAME
//                    com.delivery.sopo.util.ui_util.FragmentManager.move(activity as MainView, FragmentType.REGISTER_STEP2, (activity as MainView).frame_container.id)
                }
            }

        })
    }

}