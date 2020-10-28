package com.delivery.sopo.util

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.delivery.sopo.enums.FragmentTypeEnum

object FragmentManager
{
    var currentFragment1st = FragmentTypeEnum.REGISTER_STEP1
    var currentFragment2nd = FragmentTypeEnum.INQUIRY
    var currentFragment3rd = FragmentTypeEnum.MY_MENU

    private val TAG = "LOG.SOPO.FragmentM"


    fun move(activity: FragmentActivity, typeEnum: FragmentTypeEnum, viewId: Int)
    {
        val fm = activity.supportFragmentManager
        val transaction = fm.beginTransaction()
        transaction.run {
            replace(viewId, typeEnum.FRAGMENT, typeEnum.NAME)
            addToBackStack(null)
            commitAllowingStateLoss()
        }

        when (typeEnum.tabNo)
        {
            0 ->
            {
                currentFragment1st = typeEnum
            }
            1 ->
            {
                currentFragment2nd = typeEnum
            }
            2 ->
            {
                currentFragment3rd = typeEnum
            }
        }
    }

    fun remove(activity: FragmentActivity, fragment : Fragment)
    {
        val fm = activity.supportFragmentManager
//        fm.beginTransaction().remove(fragment).commitAllowingStateLoss()
        fm.popBackStack()

//        FragmentType.REGISTER_STEP1.NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE
    }

    // 작업이 옳은 프로세스로 진행되었을 때 프래그먼트 스택을 초기화시키고 다른 화면으로 이동하는 함수
    fun initFragment(
        activity: FragmentActivity,
        viewId: Int,
        currentFragment: Fragment,
        nextFragment: Fragment,
        nextFragmentTag: String?
    )
    {
        val fm = activity.supportFragmentManager
        var transaction = fm.beginTransaction()

        transaction.run {
            addToBackStack(null)
            remove(currentFragment)
            replace(viewId, nextFragment, nextFragmentTag)
            addToBackStack(null)
            commitAllowingStateLoss()
        }
    }
}