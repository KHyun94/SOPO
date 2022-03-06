package com.delivery.sopo.enums

class SettingEnum
{
    enum class PushAlarmType(val MESSAGE: String){
        ALWAYS("변경 있을 때마다"), ARRIVE("도착했을 때만"), REJECT("알림 안받기" +
                                                               "")
    }

}