package com.delivery.sopo.enums

import androidx.fragment.app.Fragment
import com.delivery.sopo.presentation.views.inquiry.*
import com.delivery.sopo.presentation.views.menus.*
import com.delivery.sopo.presentation.register.view.SelectCarrierFragment
import com.delivery.sopo.presentation.register.view.InputParcelFragment
import com.delivery.sopo.presentation.register.view.ConfirmParcelFragment

enum class TabCode(val TAB_NO: Int, val NAME: String, var FRAGMENT: Fragment, val TITLE:String="")
{
    REGISTER_INPUT(TabCode.REGISTER_TAB, "REGISTER_INPUT", InputParcelFragment()),
    REGISTER_SELECT(TabCode.REGISTER_TAB, "REGISTER_SELECT", SelectCarrierFragment()),
    REGISTER_CONFIRM(TabCode.REGISTER_TAB, "REGISTER_CONFIRM", ConfirmParcelFragment()),

    INQUIRY(TabCode.INQUIRY_TAB, "INQUIRY", InquiryFragment()),
    INQUIRY_ONGOING(TabCode.INQUIRY_TAB, "INQUIRY_ONGOING", OngoingTypeFragment()),
    INQUIRY_COMPLETE(TabCode.INQUIRY_TAB, "INQUIRY_COMPLETE", CompletedTypeFragment()),
    INQUIRY_DETAIL(TabCode.INQUIRY_TAB, "INQUIRY_DETAIL", ParcelDetailView()),
    DELETE_PARCEL(TabCode.INQUIRY_TAB, "INQUIRY_DELETE_PARCEL", DeleteParcelFragment()),

    MY_MENU_MAIN(TabCode.MENU_TAB, "MY_MENU_MAIN", MenuFragment()),
    MY_MENU_SUB(TabCode.MENU_TAB, "MY_MENU_SUB", MenuSubFragment()),

    MENU_NOTICE(TabCode.MENU_TAB, "MENU_NOTICE", NoticeFragment.newInstance(), "공지사항"),
    MENU_SETTING(TabCode.MENU_TAB, "MENU_SETTING", SettingFragment.newInstance(), "설정"),
    MENU_FAQ(TabCode.MENU_TAB, "MENU_FAQ", FaqFragment.newInstance(), "FAQ"),
    MENU_ACCOUNT_MANAGEMENT(TabCode.MENU_TAB, "MENU_ACCOUNT_MANAGEMENT", AccountManagerFragment.newInstance(), "계정 관리"),
    MENU_USE_TERMS(TabCode.MENU_TAB, "MENU_USE_TERMS", SettingFragment.newInstance(), "이용약관"),
    MENU_APP_INFO(TabCode.MENU_TAB, "MENU_APP_INFO", AppInfoFragment.newInstance(), "앱정보");

    companion object{
        const val REGISTER_TAB = 0
        const val INQUIRY_TAB = 1
        const val MENU_TAB = 2
    }
}
