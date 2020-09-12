package com.delivery.sopo.enums

import androidx.fragment.app.Fragment
import com.delivery.sopo.views.lookups.LookupMainFragment
import com.delivery.sopo.views.menus.MyMenuView
import com.delivery.sopo.views.menus.SettingFragment
import com.delivery.sopo.views.registers.RegisterStep2
import com.delivery.sopo.views.registers.RegisterStep1

enum class FragmentType(val tabNo: Int, val NAME: String, var FRAGMENT: Fragment)
{
    REGISTER_STEP1(0, "FRAGMENT_REGISTER_STEP1", RegisterStep1()),
    REGISTER_STEP2(0, "FRAGMENT_REGISTER_STEP2", RegisterStep2()),
    LOOKUP(1, "FRAGMENT_LOOKUP", LookupMainFragment()),
    MY_MENU(2, "FRAGMENT_MY_MENU", SettingFragment())

}