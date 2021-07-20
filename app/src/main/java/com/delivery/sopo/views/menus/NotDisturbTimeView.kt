package com.delivery.sopo.views.menus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.delivery.sopo.R
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.databinding.NotDisturbTimeViewBinding
import com.delivery.sopo.firebase.FirebaseNetwork
import com.delivery.sopo.viewmodels.menus.NotDisturbTimeViewModel
import com.delivery.sopo.views.dialog.NotDisturbTimeDialog
import com.delivery.sopo.views.widget.clockpieview.ClockPieHelper
import kotlinx.android.synthetic.main.not_disturb_time_view.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.*


class NotDisturbTimeView : AppCompatActivity()
{
    private val userLocalRepo : UserLocalRepository by inject()
    private val vm: NotDisturbTimeViewModel by viewModel()

    lateinit var binding: NotDisturbTimeViewBinding
    var clockPieHelperArrayList: ArrayList<ClockPieHelper> = ArrayList<ClockPieHelper>()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.not_disturb_time_view)
        binding.vm = vm
        binding.lifecycleOwner = this

        val startTime = vm.startTime.value?: "0000"
        val endTime = vm.endTime.value?: "0000"

        setDate(startTime, endTime)
        setObserve()

    }


    fun setObserve(){

        vm.navigator.observe(this, androidx.lifecycle.Observer { navigator ->
            when(navigator){
                NavigatorConst.TO_FLOATING_DIALOG -> {
                    showNotDisturbTimeDialog()
                }
                NavigatorConst.TO_BACK_SCREEN -> {
                    finish()
                }
            }
        })

    }

    fun showNotDisturbTimeDialog()
    {
        val startTime = vm.startTime.value?:"00:00"
        val endTime = vm.endTime.value?:"00:00"

        NotDisturbTimeDialog(act = this, startTime = startTime, endTime = endTime){ startTime, endTime ->

            setDate(startTime, endTime)

            userLocalRepo.setDisturbStartTime(startTime)
            userLocalRepo.setDisturbEndTime(endTime)

            val topicHour = endTime.substring(0, 2).toInt()
            val topicMin = endTime.substring(3, 5).toInt()

            if(userLocalRepo.getTopic().isNotEmpty())
            {
                FirebaseNetwork.unsubscribedToTopicInFCM()
            }
            FirebaseNetwork.subscribedToTopicInFCM(topicHour, topicMin)
        }.show(supportFragmentManager, "NotDisturbTimeDialog")
    }

    private fun setDate(startTime : String, endTime:String){
        vm.startTime.postValue(startTime)
        vm.endTime.postValue(endTime)

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


}