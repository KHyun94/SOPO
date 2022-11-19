package com.delivery.sopo

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*

data class DateSelector(
    val hasPrevious: Boolean = false,
    val previousDate: String? = null,
    val hasNext: Boolean = false,
    val nextDate: String? = null,
    val cursorDate: String? = null
) : Serializable {

    fun getDisplayCursorDate(): String {
        return if (cursorDate != null) {
            cursorDate.substring(0,4) +"년 " + cursorDate.substring(4,6) + "월"
        } else ""
    }
}
