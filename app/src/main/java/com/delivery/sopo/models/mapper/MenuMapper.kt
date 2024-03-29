package com.delivery.sopo.models.mapper

import android.view.Menu
import androidx.core.view.iterator
import com.delivery.sopo.data.repository.database.room.entity.ParcelCntInfoEntity
import com.delivery.sopo.models.inquiry.InquiryMenuItem
import com.delivery.sopo.util.SopoLog

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

    fun timeCountDtoToMenuItemList(timeCntDtoList: MutableList<ParcelCntInfoEntity>): List<InquiryMenuItem>{
        val menuItemList = mutableListOf<InquiryMenuItem>()
        for(timeCnt in timeCntDtoList){
            menuItemList.add(InquiryMenuItem(viewType = InquiryMenuItem.InquiryMenuType.CompleteHistoryList,
                                             parcelCntInfo = timeCnt))
        }
        return menuItemList
    }

    fun timeToListTitle(time: String): String{

        SopoLog.d("time >>> $time")

        try
        {
            val times = time.split("-").toMutableList()
            times[0] += "년"

            times[1] = if(times[1].startsWith("0"))
            {
                times[1].replace("0", "") + "월"
            }
            else
            {
                times[1] + "월"
            }

            return times[0] + " " + times[1]
        }
        catch(e: Exception)
        {
            SopoLog.e("에러 발생 >>> $e", e)
            return "0000년 00월"
        }
    }

    fun timeToInquiryDate(time: String): String{
        return time.replace("-","")
    }

    fun titleToInquiryDate(title: String): String{
        return title.replace("년 ", "").replace("월", "")
    }
}