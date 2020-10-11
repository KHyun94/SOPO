package com.delivery.sopo.interfaces.notification

import android.content.Context
import android.content.Intent
import com.delivery.sopo.models.parcel.Parcel
import com.google.firebase.messaging.RemoteMessage

interface Notification {
    fun alertUpdateParcel(remoteMessage: RemoteMessage, context: Context, intent: Intent, parcel: Parcel, newDeliveryStatus: String)
}