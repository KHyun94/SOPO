package com.delivery.sopo.enums

import androidx.fragment.app.Fragment
import com.delivery.sopo.views.lookups.LookupMain
import com.delivery.sopo.views.menus.MyMenuView
import com.delivery.sopo.views.registers.RegisterDeliveryCo
import com.delivery.sopo.views.registers.RegisterTrackNum

enum class FragmentType(val NAME:String,val FRAGMENT: Fragment)
{
    REGISTER_STEP1("FRAGMENT_REGISTER_STEP1", RegisterTrackNum()),
    REGISTER_STEP2("FRAGMENT_REGISTER_STEP2", RegisterDeliveryCo()),
    LOOKUP("FRAGMENT_LOOKUP", LookupMain()),
    MY_MENU("FRAGMENT_MY_MENU", MyMenuView())

}