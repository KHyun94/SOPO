package com.delivery.sopo.interfaces

import com.delivery.sopo.enums.TabCode

interface OnPageSelectListener
{
    fun onChangeTab(tab: TabCode?)
    fun onSetCurrentPage(page: Int)
}