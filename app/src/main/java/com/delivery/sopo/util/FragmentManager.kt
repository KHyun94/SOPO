package com.delivery.sopo.util

import android.widget.Toast
import androidx.annotation.IdRes
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.delivery.sopo.enums.TabCode
import com.google.android.material.tabs.TabLayout

object FragmentManager
{
    fun add(activity: FragmentActivity, code: TabCode, @IdRes viewId: Int)
    {
        SopoLog.d("add() 호출:[Fragment:${code.NAME}][viewId:$viewId]")

        val fm = activity.supportFragmentManager
        val transaction = fm.beginTransaction()

        transaction.add(viewId, code.FRAGMENT, code.NAME).addToBackStack(null).commit()
    }

    fun refreshMove(activity: FragmentActivity, code: TabCode, viewId:Int)
    {
        val fm = activity.supportFragmentManager
        fm.popBackStack()

        val transaction = fm.beginTransaction()

        transaction.run {
            replace(viewId, code.FRAGMENT, code.NAME)
            commit()
        }
    }

    fun move(activity: FragmentActivity, code: TabCode, @IdRes viewId: Int, isAdd: Boolean = false)
    {

        SopoLog.d("move() 호출:[Fragment:${code.NAME}][viewId:$viewId] ${activity.localClassName} ${activity.componentName}")

        val fm = activity.supportFragmentManager
        val transaction = fm.beginTransaction()

        transaction.run {
            replace(viewId, code.FRAGMENT, code.NAME)
            if(isAdd) addToBackStack(null)
//            commitAllowingStateLoss()
//            return Toast.makeText(activity, "isDestroyed", Toast.LENGTH_SHORT).show()
            if(fm.isDestroyed)
            {
                commitAllowingStateLoss()
                return Toast.makeText(activity, "isDestroyed", Toast.LENGTH_SHORT).show()
            }
            else
            {
                commit()
            }

        }
    }

    fun remove(activity: FragmentActivity)
    {
        val fm = activity.supportFragmentManager
        fm.popBackStack()
    }

    fun clearBackStack(fragmentManager: FragmentManager) {
        with(fragmentManager) {
            if (backStackEntryCount > 0)
                popBackStack()
        }
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