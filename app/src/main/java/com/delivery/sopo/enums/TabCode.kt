package com.delivery.sopo.enums

import androidx.fragment.app.Fragment
import com.delivery.sopo.views.inquiry.ParcelDetailView
import com.delivery.sopo.views.inquiry.InquiryMainFrame
import com.delivery.sopo.views.inquiry.InquiryView
import com.delivery.sopo.views.menus.MenuFragment
import com.delivery.sopo.views.registers.RegisterStep2
import com.delivery.sopo.views.registers.RegisterStep1
import com.delivery.sopo.views.registers.RegisterStep3

enum class TabCode(val tabNo: Int, val NAME: String, var FRAGMENT: Fragment)
{
    REGISTER_STEP1(0, "FRAGMENT_REGISTER_STEP1", RegisterStep1()),
    REGISTER_STEP2(0, "FRAGMENT_REGISTER_STEP2", RegisterStep2()),
    REGISTER_STEP3(0, "FRAGMENT_REGISTER_STEP3", RegisterStep3()),

    INQUIRY_DETAIL(1, "FRAGMENT_INQUIRY_DETAIL", ParcelDetailView()),
    INQUIRY(1,"FRAGMENT_LOOKUP", InquiryView()),
    MY_MENU(2, "FRAGMENT_MY_MENU", MenuFragment())
}