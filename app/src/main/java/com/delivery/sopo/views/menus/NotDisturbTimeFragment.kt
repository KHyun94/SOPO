package com.delivery.sopo.views.menus

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentNotDisturbTimeBinding
import com.delivery.sopo.viewmodels.menus.NotDisturbTimeViewModel
import com.delivery.sopo.views.dialog.NotDisturbTimeDialog
import com.delivery.sopo.views.widget.clockpieview.ClockPieHelper
import kotlinx.android.synthetic.main.fragment_not_disturb_time.view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class NotDisturbTimeFragment : Fragment()
{
    private val notDisturbVm : NotDisturbTimeViewModel by viewModel()

    lateinit var binding: FragmentNotDisturbTimeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_not_disturb_time,
            container,
            false
        )
        binding.vm = notDisturbVm
        binding.lifecycleOwner = this

        // TODO 테스트
        val clockPieHelperArrayList: ArrayList<ClockPieHelper> = ArrayList<ClockPieHelper>()
        clockPieHelperArrayList.add(
            ClockPieHelper(
                22,
                0,
                6,
                0
            )
        )
        binding.root.pie_view.setDate(clockPieHelperArrayList)

        setObserver()
        setListener()

        return binding.root
    }

    fun setListener(){
        binding.root.constraint_not_disturb_time_board.setOnClickListener {
            NotDisturbTimeDialog(this.requireActivity()).show(requireActivity().supportFragmentManager, "NotDisturbTimeDialog")
        }
    }

    fun setObserver()
    {
    }
}