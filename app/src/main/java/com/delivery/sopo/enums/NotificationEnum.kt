package com.delivery.sopo.enums

enum class NotificationEnum(val notificationId: String, val channelName: String) {
    PUSH_UPDATE_PARCEL(notificationId = "10001", channelName = "PUSH_UPDATE_PARCEL"),
    PUSH_FRIEND_RECOMMEND(notificationId = "20001", channelName = "PUSH_FRIEND_RECOMMEND"),
    PUSH_AWAKEN_DEVICE(notificationId = "30001", channelName = "PUSH_AWAKEN_DEVICE"),
    PUSH_IMPORTANT_NOTICE(notificationId = "90001", channelName = "PUSH_IMPORTANT_NOTICE"),
}