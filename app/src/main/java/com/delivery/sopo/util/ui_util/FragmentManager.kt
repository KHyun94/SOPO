package com.delivery.sopo.util.ui_util

import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.delivery.sopo.enums.FragmentType

object FragmentManager
{
    var fm: FragmentManager? = null

    private val TAG = "LOG.SOPO.FragmentM"
    private val FRONT_FRAGMENT = 0
    private val BACK_FRAGMENT = 1
    private val NO_EXIST_FRAGMENT = 2

    fun move(act: AppCompatActivity, type: FragmentType, viewId: Int)
    {
        if (fm == null)
            fm = act.supportFragmentManager

        val transaction = fm?.beginTransaction()

        val list: MutableList<Fragment>? = act.supportFragmentManager.fragments

        val state =
            if (list == null || list.size == 0)
                NO_EXIST_FRAGMENT
            else isState(type, list)

        Log.d(TAG, "State ~~~~ ${state}")

        when (state)
        {
            NO_EXIST_FRAGMENT ->
            {

                Log.d(TAG, "No exist Fragment")

                transaction?.run {
                    add(viewId, type.FRAGMENT, type.NAME)

                    if (list!!.size > 0)
                        hide(getCurrentFragment(list)!!)

//                    addToBackStack(null)
                    commit()
                }
            }
            FRONT_FRAGMENT ->
            {
                Log.d(TAG, "Front Fragment")
            }
            BACK_FRAGMENT ->
            {
                Log.d(TAG, "Back Fragment")
                transaction?.run {
                    hide(getCurrentFragment(list!!)!!)
                    show(type.FRAGMENT)
//                    addToBackStack(null)
                    commit()
                }
            }
        }
    }

    fun remove(act: AppCompatActivity, type: FragmentType)
    {
        val transaction = act.supportFragmentManager.beginTransaction()
        transaction.remove(type.FRAGMENT)
        transaction.commit()
    }

    fun moveToNextStep(act: AppCompatActivity,type: FragmentType, viewId: Int){
        val transaction = act.supportFragmentManager.beginTransaction()
        transaction.replace(viewId, type.FRAGMENT, type.NAME)
            .commit()
    }

    fun getCurrentFragment(list: MutableList<Fragment>): Fragment?
    {
        for (fragment in list)
        {
            if (fragment.isVisible)
            {
                return fragment
            }
        }

        return null
    }

    fun isState(type: FragmentType, list: MutableList<Fragment>): Int
    {
        val TAG = "LOG.SOPO.STATE"
        for (fragment in list)
        {
            if (fragment.isVisible)
            {
                if (fragment.tag == type.NAME)
                {
                    Log.d(TAG, "Front Fragment ${fragment.tag}")
                    return FRONT_FRAGMENT
                }
            }
            else
            {
                if (fragment.tag == type.NAME)
                {
                    Log.d(TAG, "Back Fragment ${fragment.tag}")
                    return BACK_FRAGMENT
                }
            }
        }

        return NO_EXIST_FRAGMENT
    }

    fun nextFragment(act: AppCompatActivity, type: FragmentType, viewId: Int)
    {
        val transaction = act.supportFragmentManager.beginTransaction()
        transaction
            .replace(viewId, type.FRAGMENT, type.NAME)
            .addToBackStack(null)
            .commit()
    }

    fun testFragment(act: AppCompatActivity, type: FragmentType, viewId: Int)
    {
        val transaction = act.supportFragmentManager.beginTransaction()

        val list: MutableList<Fragment> = act.supportFragmentManager.fragments

        Log.d(TAG, "${getCurrentFragment(list)!!}")

        transaction
            .replace(viewId, type.FRAGMENT, type.NAME)
            .commit()
    }

}