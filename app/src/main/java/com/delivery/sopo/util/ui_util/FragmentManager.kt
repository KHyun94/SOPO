package com.delivery.sopo.util.ui_util

import android.util.Log
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.delivery.sopo.enums.FragmentType

object FragmentManager
{
    var currentFragment1st = FragmentType.REGISTER_STEP1
    var currentFragment2nd = FragmentType.LOOKUP
    var currentFragment3rd = FragmentType.MY_MENU

    private val TAG = "LOG.SOPO.FragmentM"

    fun move(activity: FragmentActivity, type: FragmentType, viewId: Int)
    {
        val fm = activity.supportFragmentManager
        val transaction = fm.beginTransaction()
        transaction.run {
            replace(viewId, type.FRAGMENT, type.NAME)
            addToBackStack(null)
            commit()
        }

        when (type.tabNo)
        {
            0 ->
            {
                currentFragment1st = type
            }
            1 ->
            {
                currentFragment2nd = type
            }
            2 ->
            {
                currentFragment3rd = type
            }
        }
    }

    fun remove(activity: FragmentActivity){
        val fm = activity.supportFragmentManager
        fm.popBackStack()
//        FragmentType.REGISTER_STEP1.NAME, FragmentManager.POP_BACK_STACK_INCLUSIVE
    }

//    fun move(act: AppCompatActivity, type: FragmentType, viewId: Int)
//    {
//        if (fm == null)
//            fm = act.supportFragmentManager
//
//        val transaction = fm?.beginTransaction()
//
//        val list: MutableList<Fragment>? = act.supportFragmentManager.fragments
//
//        val state =
//            if (list == null || list.size == 0)
//                NO_EXIST_FRAGMENT
//            else isState(type, list)
//
//        Log.d(TAG, "State ~~~~ ${state}")
//
//        when (state)
//        {
//            NO_EXIST_FRAGMENT ->
//            {
//
//                Log.d(TAG, "No exist Fragment")
//
//                transaction?.run {
//                    add(viewId, type.FRAGMENT, type.NAME)
//
//                    if (list!!.size > 0)
//                        hide(getCurrentFragment(list)!!)
//
////                    addToBackStack(null)
//                    commit()
//                }
//            }
//            FRONT_FRAGMENT ->
//            {
//                Log.d(TAG, "Front Fragment")
//            }
//            BACK_FRAGMENT ->
//            {
//                Log.d(TAG, "Back Fragment")
//                transaction?.run {
//                    hide(getCurrentFragment(list!!)!!)
//                    show(type.FRAGMENT)
////                    addToBackStack(null)
//                    commit()
//                }
//            }
//        }
//    }
//
//    fun remove(act: AppCompatActivity, type: FragmentType)
//    {
//        val transaction = act.supportFragmentManager.beginTransaction()
//        transaction.remove(type.FRAGMENT)
//        transaction.commit()
//    }
//
//    fun moveToNextStep(act: AppCompatActivity,type: FragmentType, viewId: Int){
//        val transaction = act.supportFragmentManager.beginTransaction()
//        transaction.replace(viewId, type.FRAGMENT, type.NAME)
//            .commit()
//    }
//
//    fun getCurrentFragment(list: MutableList<Fragment>): Fragment?
//    {
//        for (fragment in list)
//        {
//            if (fragment.isVisible)
//            {
//                return fragment
//            }
//        }
//
//        return null
//    }
//
//    fun isState(type: FragmentType, list: MutableList<Fragment>): Int
//    {
//        val TAG = "LOG.SOPO.STATE"
//        for (fragment in list)
//        {
//            if (fragment.isVisible)
//            {
//                if (fragment.tag == type.NAME)
//                {
//                    Log.d(TAG, "Front Fragment ${fragment.tag}")
//                    return FRONT_FRAGMENT
//                }
//            }
//            else
//            {
//                if (fragment.tag == type.NAME)
//                {
//                    Log.d(TAG, "Back Fragment ${fragment.tag}")
//                    return BACK_FRAGMENT
//                }
//            }
//        }
//
//        return NO_EXIST_FRAGMENT
//    }
//
//    fun nextFragment(act: AppCompatActivity, type: FragmentType, viewId: Int)
//    {
//        val transaction = act.supportFragmentManager.beginTransaction()
//        transaction
//            .replace(viewId, type.FRAGMENT, type.NAME)
//            .addToBackStack(null)
//            .commit()
//    }
//
//    fun testFragment(act: AppCompatActivity, type: FragmentType, viewId: Int)
//    {
//        val transaction = act.supportFragmentManager.beginTransaction()
//
//        val list: MutableList<Fragment> = act.supportFragmentManager.fragments
//
//        Log.d(TAG, "${getCurrentFragment(list)!!}")
//
//        transaction
//            .replace(viewId, type.FRAGMENT, type.NAME)
//            .commit()
//    }

}