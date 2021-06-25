package com.delivery.sopo.util

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.delivery.sopo.enums.TabCode

object FragmentManager
{
    var currentFragment1st = TabCode.REGISTER_INPUT
    var currentFragment2nd = TabCode.INQUIRY
    var currentFragment3rd = TabCode.MY_MENU_MAIN

    fun move(activity: FragmentActivity, code: TabCode, @IdRes viewId: Int)
    {
        SopoLog.d("move() call >>> ${code.NAME} / $viewId")

        val fm = activity.supportFragmentManager
        val transaction = fm.beginTransaction()
        transaction.run {
            replace(viewId, code.FRAGMENT, code.NAME)
            addToBackStack(null)
            commitAllowingStateLoss()
        }

        when (code.tabNo)
        {
            0 ->
            {
                currentFragment1st = code
            }
            1 ->
            {
                currentFragment2nd = code
            }
            2 ->
            {
                currentFragment3rd = code
            }
        }
    }

    fun remove(activity: FragmentActivity)
    {
        val fm = activity.supportFragmentManager
        fm.popBackStack()
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