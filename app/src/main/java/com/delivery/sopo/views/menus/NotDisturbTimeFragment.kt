package com.delivery.sopo.views.menus

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.delivery.sopo.R
import com.delivery.sopo.databinding.FragmentNotDisturbTimeBinding
import com.delivery.sopo.firebase.FirebaseNetwork
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.enums.TabCode
import com.delivery.sopo.util.FragmentManager
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.viewmodels.menus.NotDisturbTimeViewModel
import com.delivery.sopo.views.dialog.NotDisturbTimeDialog
import com.delivery.sopo.views.main.MainView
import com.delivery.sopo.views.widget.clockpieview.ClockPieHelper
import kotlinx.android.synthetic.main.fragment_not_disturb_time.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class NotDisturbTimeFragment : Fragment()
{
    private val userLocalRepository : UserLocalRepository by inject()
    private val vm: NotDisturbTimeViewModel by viewModel()

    lateinit var binding: FragmentNotDisturbTimeBinding
    private lateinit var parentView: MainView
    var clockPieHelperArrayList: ArrayList<ClockPieHelper> = ArrayList<ClockPieHelper>()

    lateinit var callback: OnBackPressedCallback

    override fun onAttach(context: Context)
    {
        super.onAttach(context)

        callback = object: OnBackPressedCallback(true)
        {
            override fun handleOnBackPressed()
            {
                FragmentManager.move(parentView, TabCode.MENU_SETTING, MenuSubFragment.viewId)
            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(this, callback)
    }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        SopoLog.d("child cnt:${childFragmentManager.fragments.size}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
    {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_not_disturb_time, container, false)
        binding.vm = vm
        binding.lifecycleOwner = this

        parentView = activity as MainView

        val startTime = vm.startTime.value?: "0000"
        val endTime = vm.endTime.value?: "0000"

        setDate(startTime, endTime)
        setListener()

        return binding.root
    }

    fun setListener()
    {
        binding.root.constraint_not_disturb_time_board.setOnClickListener {

            val startTime = binding.vm!!.startTime.value?:"00:00"
            val endTime = binding.vm!!.endTime.value?:"00:00"

            NotDisturbTimeDialog(act = this.requireActivity(), startTime = startTime, endTime = endTime){ startTime, endTime ->

                setDate(startTime, endTime)

                SopoLog.d( msg = """
                    start Time >>> $startTime
                    end Time >>> $endTime
                """.trimIndent())

                userLocalRepository.setDisturbStartTime(startTime)
                userLocalRepository.setDisturbEndTime(endTime)

                val topicHour = endTime.substring(0, 2).toInt()
                val topicMin = endTime.substring(3, 5).toInt()

                if(userLocalRepository.getTopic().isEmpty())
                {
                    FirebaseNetwork.subscribedToTopicInFCM(topicHour, topicMin)
                }
                else
                {
                    FirebaseNetwork.unsubscribedToTopicInFCM()
                }
            }.show(
                requireActivity().supportFragmentManager,
                "NotDisturbTimeDialog"
            )
        }
    }

    fun setDate(startTime : String, endTime:String){
        binding.vm!!.startTime.postValue(startTime)
        binding.vm!!.endTime.postValue(endTime)

        val startTimeList = startTime.split(":")
        val endTimeList = endTime.split(":")

        val startHour = startTimeList[0].toInt()
        val startMin = startTimeList[1].toInt()

        val endHour = endTimeList[0].toInt()
        val endMin = endTimeList[1].toInt()

        if(clockPieHelperArrayList.size > 0) clockPieHelperArrayList[0] = ClockPieHelper(startHour, startMin, endHour, endMin)
        else clockPieHelperArrayList.add(ClockPieHelper(startHour, startMin, endHour, endMin))

        binding.root.pie_view.setDate(clockPieHelperArrayList)
    }

    override fun onDetach()
    {
        super.onDetach()

        callback.remove()
    }
}