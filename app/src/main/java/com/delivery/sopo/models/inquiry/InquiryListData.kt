package com.delivery.sopo.models.inquiry

import com.delivery.sopo.models.parcel.Parcel
import java.text.SimpleDateFormat
import java.util.*


class InquiryListData(
    val parcel: Parcel,
    var isSelected: Boolean = false
){
    val calendar: Calendar by lazy {
        val cal = Calendar.getInstance()
        cal.time = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).parse(getParseString())
        cal
    }

    private fun getParseString(): String{
        return parcel.arrivalDte?.let {
            it.substring(0, it.indexOf("T"))
        } ?: ""
    }

    fun getDateOfMonth(): String{
        return "${calendar.get(Calendar.DATE)}"
    }

    fun getDayOfWeek(): String{

        return when(calendar.get(Calendar.DAY_OF_WEEK))
        {
            1-> {
                 "일"
            }
            2-> {
                 "월"
            }
            3-> {
                 "화"
            }
            4-> {
                 "수"
            }
            5-> {
                 "목"
            }
            6-> {
                 "금"
            }
            7-> {
                 "토"
            }
            else -> {
                 ""
            }
        }
    }
}
