package com.delivery.sopo.util

import androidx.annotation.IdRes
import androidx.fragment.app.FragmentActivity
import com.delivery.sopo.enums.TabCode

object FragmentManagerBeta
{
    fun add(activity: FragmentActivity, code: TabCode, @IdRes viewId: Int)
    {
        SopoLog.d("add() 호출:[Fragment:${code.NAME}][viewId:$viewId]")

        val fm = activity.supportFragmentManager
        val transaction = fm.beginTransaction()

    /*    if(code.FRAGMENT.isAdded)
        {
            transaction.remove(code.FRAGMENT)
            SopoLog.d("중첩 프래그먼트[${code}] 존재 삭제 중? : ${code.FRAGMENT.isRemoving}")
        }
*/
        transaction.run {
            add(viewId, code.FRAGMENT, code.NAME)
            addToBackStack(null)
            commit()
        }
    }

    fun remove(activity: FragmentActivity)
    {
        val fm = activity.supportFragmentManager
        fm.popBackStack()
    }

}