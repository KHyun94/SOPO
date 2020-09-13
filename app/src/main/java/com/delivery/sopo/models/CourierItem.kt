package com.delivery.sopo.models

import java.io.Serializable

data class CourierItem(val courierName: String, val clickRes: Int, val nonClickRes: Int, val iconRes : Int) : Serializable