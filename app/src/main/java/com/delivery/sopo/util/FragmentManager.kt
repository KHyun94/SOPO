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
            commit()
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

    fun remove(activity: FragmentActivity)
    {
        val fm = activity.supportFragmentManager
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
            commit()
        }
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