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
    REGISTER_INPUT(0, "FRAGMENT_REGISTER_STEP1", InputParcelFragment()),
    REGISTER_SELECT(0, "FRAGMENT_REGISTER_STEP2", SelectCarrierFragment()),
    REGISTER_CONFIRM(0, "FRAGMENT_REGISTER_STEP3", ConfirmParcelFragment()),

    INQUIRY(1, "FRAGMENT_INQUIRY", InquiryFragment()),
    INQUIRY_DETAIL(1, "FRAGMENT_INQUIRY_DETAIL", ParcelDetailView()),

    MY_MENU_MAIN(2, "FRAGMENT_MY_MENU_MAIN", MenuFragment()),
    MY_MENU_SUB(2, "FRAGMENT_MY_MENU_SUB", MenuSubFragment()),

    MENU_NOTICE(2, "공지사항", NoticeFragment()),
    MENU_SETTING(2, "설정", SettingFragment()),
    MENU_FAQ(2, "FAQ", FaqFragment()),
    MENU_ACCOUNT_MANAGEMENT(2, "계정 관리", AccountManagerFragment()),
    MENU_USE_TERMS(2, "이용약관", SettingFragment()),
    MENU_NOT_DISTURB(2, "방해금지 시간대 설정", NotDisturbTimeFragment()),
    MENU_APP_INFO(2, "앱 정보", AppInfoFragment())
}