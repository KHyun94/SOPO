package com.delivery.sopo.util

import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.delivery.sopo.enums.TabCode
import com.google.android.material.tabs.TabLayout

object FragmentManager
{
    fun add(activity: FragmentActivity, code: TabCode, @IdRes viewId: Int)
    {
        SopoLog.d("add() 호출:[Fragment:${code.NAME}][viewId:$viewId]")

        val fm = activity.supportFragmentManager
        val transaction = fm.beginTransaction()

        transaction.add(viewId, code.FRAGMENT, code.NAME)
            .addToBackStack(null)
            .commit()
    }

    fun move(activity: FragmentActivity, code: TabCode, @IdRes viewId: Int)
    {
        SopoLog.d("move() 호출:[Fragment:${code.NAME}][viewId:$viewId]")

        val fm = activity.supportFragmentManager
        val transaction = fm.beginTransaction()

/*
        if(code.FRAGMENT.isAdded)
        {
            transaction.remove(code.FRAGMENT)
            SopoLog.d("중첩 프래그먼트[${code}] 존재 삭제 중? : ${code.FRAGMENT.isRemoving}")
        }
*/
        when(code.TAB_NO)
        {
            TabCode.firstTab ->
            {
            }
            TabCode.secondTab ->
            {

            }
            TabCode.thirdTab ->
            {

            }
        }

        transaction.run {
            replace(viewId, code.FRAGMENT, code.NAME)
//            addToBackStack(null)
            commit()
        }
    }

    fun remove(activity: FragmentActivity)
    {
        val fm = activity.supportFragmentManager
        fm.popBackStack()
    }

    // 작업이 옳은 프로세스로 진행되었을 때 프래그먼트 스택을 초기화시키고 다른 화면으로 이동하는 함수
    fun initFragment(activity: FragmentActivity, viewId: Int, currentFragment: Fragment, nextFragment: Fragment, nextFragmentTag: String?)
    {
        val fm = activity.supportFragmentManager
        val transaction = fm.beginTransaction()

        transaction.run {
            addToBackStack(null)
            remove(currentFragment)
            replace(viewId, nextFragment, nextFragmentTag)
            addToBackStack(null)
            commitAllowingStateLoss()
        }
    }
}