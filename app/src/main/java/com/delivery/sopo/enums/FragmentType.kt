package com.delivery.sopo.enums

import androidx.fragment.app.Fragment
import com.delivery.sopo.consts.FragmentConst
import com.delivery.sopo.views.lookups.LookupMain
import com.delivery.sopo.views.menus.MyMenuView
import com.delivery.sopo.views.registers.RegisterTrackNum

enum class FragmentType(val NAME:String,val FRAGMENT: Fragment)
{
    REGISTER(FragmentConst.FRAGMENT_REGISTER, RegisterTrackNum()),
    LOOKUP(FragmentConst.FRAGMENT_LOOKUP, LookupMain()),
    MY_MENU(FragmentConst.FRAGMENT_MY_MENU, MyMenuView())

}