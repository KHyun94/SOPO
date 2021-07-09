package com.delivery.sopo.enums

import androidx.fragment.app.Fragment
import com.delivery.sopo.views.inquiry.ParcelDetailView
import com.delivery.sopo.views.inquiry.InquiryFragment
import com.delivery.sopo.views.menus.*
import com.delivery.sopo.views.registers.SelectCarrierFragment
import com.delivery.sopo.views.registers.InputParcelFragment
import com.delivery.sopo.views.registers.ConfirmParcelFragment

enum class TabCode(val tabNo: Int, val NAME: String, var FRAGMENT: Fragment)
{
    REGISTER_INPUT(TabCode.firstTab, "REGISTER_INPUT", InputParcelFragment()),
    REGISTER_SELECT(TabCode.firstTab, "FRAGMENT_REGISTER_STEP2", SelectCarrierFragment()),
    REGISTER_CONFIRM(TabCode.firstTab, "FRAGMENT_REGISTER_STEP3", ConfirmParcelFragment()),

    INQUIRY(TabCode.secondTab, "FRAGMENT_INQUIRY", InquiryFragment()),
    INQUIRY_DETAIL(TabCode.secondTab, "FRAGMENT_INQUIRY_DETAIL", ParcelDetailView()),

    MY_MENU_MAIN(TabCode.thirdTab, "FRAGMENT_MY_MENU_MAIN", MenuFragment()),
    MY_MENU_SUB(TabCode.thirdTab, "FRAGMENT_MY_MENU_SUB", MenuSubFragment()),

    MENU_NOTICE(TabCode.thirdTab, "공지사항", NoticeFragment()),
    MENU_SETTING(TabCode.thirdTab, "설정", SettingFragment()),
    MENU_FAQ(TabCode.thirdTab, "FAQ", FaqFragment()),
    MENU_ACCOUNT_MANAGEMENT(TabCode.thirdTab, "계정 관리", AccountManagerFragment()),
    MENU_USE_TERMS(TabCode.thirdTab, "이용약관", SettingFragment()),
    MENU_APP_INFO(TabCode.thirdTab, "앱 정보", AppInfoFragment());

    companion object{
        const val firstTab = 0
        const val secondTab = 1
        const val thirdTab = 2
    }
}
