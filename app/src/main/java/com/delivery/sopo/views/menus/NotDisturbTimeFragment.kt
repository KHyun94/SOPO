package com.delivery.sopo.views.menus

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.observe
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentNotDisturbTimeBinding
import com.delivery.sopo.viewmodels.menus.NotDisturbTimeViewModel
import com.delivery.sopo.views.dialog.NotDisturbTimeDialog
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.widget.clockpieview.ClockPieHelper
import kotlinx.android.synthetic.main.fragment_not_disturb_time.view.*
import kotlinx.android.synthetic.main.menu_view.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class NotDisturbTimeFragment : Fragment()
{
    private val notDisturbVm: NotDisturbTimeViewModel by viewModel()

    lateinit var binding: FragmentNotDisturbTimeBinding
    private lateinit var parentView: MainView

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

        parentView = activity as MainView

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

    fun setListener()
    {
        binding.root.constraint_not_disturb_time_board.setOnClickListener {
            NotDisturbTimeDialog(this.requireActivity()).show(
                requireActivity().supportFragmentManager,
                "NotDisturbTimeDialog"
            )
        }

    }

    fun setObserver()
    {

    }

}