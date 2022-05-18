package com.delivery.sopo.enums

import androidx.fragment.app.Fragment
import com.delivery.sopo.presentation.views.inquiry.*
import com.delivery.sopo.presentation.views.menus.*
import com.delivery.sopo.presentation.views.registers.SelectCarrierFragment
import com.delivery.sopo.presentation.views.registers.InputParcelFragment
import com.delivery.sopo.presentation.views.registers.ConfirmParcelFragment

enum class TabCode(val TAB_NO: Int, val NAME: String, var FRAGMENT: Fragment, val TITLE:String="")
{
    REGISTER_INPUT(TabCode.firstTab, "REGISTER_INPUT", InputParcelFragment()),
    REGISTER_SELECT(TabCode.firstTab, "REGISTER_SELECT", SelectCarrierFragment()),
    REGISTER_CONFIRM(TabCode.firstTab, "REGISTER_CONFIRM", ConfirmParcelFragment()),

    INQUIRY(TabCode.secondTab, "INQUIRY", InquiryFragment()),
    INQUIRY_ONGOING(TabCode.secondTab, "INQUIRY_ONGOING", OngoingTypeFragment()),
    INQUIRY_COMPLETE(TabCode.secondTab, "INQUIRY_COMPLETE", CompletedTypeFragment()),
    INQUIRY_DETAIL(TabCode.secondTab, "INQUIRY_DETAIL", ParcelDetailView()),
    DELETE_PARCEL(TabCode.secondTab, "INQUIRY_DELETE_PARCEL", DeleteParcelFragment()),

    MY_MENU_MAIN(TabCode.thirdTab, "MY_MENU_MAIN", MenuFragment()),
    MY_MENU_SUB(TabCode.thirdTab, "MY_MENU_SUB", MenuSubFragment()),

    MENU_NOTICE(TabCode.thirdTab, "MENU_NOTICE", NoticeFragment.newInstance(), "공지사항"),
    MENU_SETTING(TabCode.thirdTab, "MENU_SETTING", SettingFragment.newInstance(), "설정"),
    MENU_FAQ(TabCode.thirdTab, "MENU_FAQ", FaqFragment.newInstance(), "FAQ"),
    MENU_ACCOUNT_MANAGEMENT(TabCode.thirdTab, "MENU_ACCOUNT_MANAGEMENT", AccountManagerFragment.newInstance(), "계정 관리"),
    MENU_USE_TERMS(TabCode.thirdTab, "MENU_USE_TERMS", SettingFragment.newInstance(), "이용약관"),
    MENU_APP_INFO(TabCode.thirdTab, "MENU_APP_INFO", AppInfoFragment.newInstance(), "앱정보");

    companion object{
        const val firstTab = 0
        const val secondTab = 1
        const val thirdTab = 2
    }
}
