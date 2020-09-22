package com.delivery.sopo.models.inquiry

import com.delivery.sopo.database.dto.TimeCountDTO

class MenuItem(
    val viewType: MenuType,
    val timeCount: TimeCountDTO? = null,
    val menuTitle: String? = null
){
    enum class MenuType
    {
        MainMenu, CompleteHistoryList
    }
}