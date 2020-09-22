package com.delivery.sopo.mapper

import android.view.Menu
import androidx.core.view.iterator
import com.delivery.sopo.database.dto.TimeCountDTO
import com.delivery.sopo.models.entity.ParcelEntity
import com.delivery.sopo.models.inquiry.MenuItem
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId

object MenuMapper
{
    fun menuToMenuItemList(menu: Menu): List<MenuItem>{

        val menuItemList = mutableListOf<MenuItem>()
        for (menuItem in menu){
            menuItemList.add(MenuItem(viewType = MenuItem.MenuType.MainMenu,
                                      menuTitle = menuItem.title as String))
        }
        return menuItemList
    }

    fun timeCountDtoToMenuItemList(timeCntDtoList: MutableList<TimeCountDTO>): List<MenuItem>{
        val menuItemList = mutableListOf<MenuItem>()
        for(timeCnt in timeCntDtoList){
            menuItemList.add(MenuItem(viewType = MenuItem.MenuType.CompleteHistoryList,
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
}