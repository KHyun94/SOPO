package com.delivery.sopo.mapper

import android.view.Menu
import androidx.core.view.iterator
import com.delivery.sopo.database.room.entity.TimeCountEntity
import com.delivery.sopo.models.inquiry.InquiryMenuItem

object MenuMapper
{
    fun menuToMenuItemList(menu: Menu): List<InquiryMenuItem>{

        val menuItemList = mutableListOf<InquiryMenuItem>()
        for (menuItem in menu){
            menuItemList.add(InquiryMenuItem(viewType = InquiryMenuItem.InquiryMenuType.MainMenu,
                                      menuTitle = menuItem.title as String))
        }
        return menuItemList
    }

    fun timeCountDtoToMenuItemList(timeCntDtoList: MutableList<TimeCountEntity>): List<InquiryMenuItem>{
        val menuItemList = mutableListOf<InquiryMenuItem>()
        for(timeCnt in timeCntDtoList){
            menuItemList.add(InquiryMenuItem(viewType = InquiryMenuItem.InquiryMenuType.CompleteHistoryList,
                                             timeCount = timeCnt))
        }
        return menuItemList
    }

    fun timeToListTitle(time: String): String{
        return time.replace("-", "년 ") + "월"
    }

    fun timeToInquiryDate(time: String): String{
        return time.replace("-","")
    }

    fun titleToInquiryDate(title: String): String{
        return title.replace("년 ", "").replace("월", "")
    }
}