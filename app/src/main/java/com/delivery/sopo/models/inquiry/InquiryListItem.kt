package com.delivery.sopo.models.inquiry

import com.delivery.sopo.enums.InquiryItemTypeEnum
import com.delivery.sopo.models.parcel.Parcel
import java.text.SimpleDateFormat
import java.util.*

class InquiryListItem(
    val parcel: Parcel,
    var isSelected: Boolean = false,
    val viewTypeEnum: InquiryItemTypeEnum? = null
){
    val completeTimeDate: Calendar by lazy {
        val cal = Calendar.getInstance()
        parcel.arrivalDte?.let {
            cal.time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(parcel!!.arrivalDte!!.replace("T", " "))
        }
        cal
    }
    val ongoingTimeDate: Calendar by lazy {
        val cal = Calendar.getInstance()
        parcel.auditDte.let {
            cal.time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(parcel.auditDte.replace("T", " "))
        }
        cal
    }

    private fun getParseString(): String{
        return parcel.arrivalDte?.let {
            it.substring(0, it.indexOf("T"))
        } ?: " "
    }

    fun getCompleteYearMonth(): String{
        return "${completeTimeDate.get(Calendar.YEAR)}/${completeTimeDate.get(Calendar.MONTH)+1}"
    }

    fun getCompleteDateTime(): String{
        return "${completeTimeDate.get(Calendar.YEAR)}/${completeTimeDate.get(Calendar.MONTH)+1}/${completeTimeDate.get(Calendar.DATE)} ${String.format("%02d", completeTimeDate.get(Calendar.HOUR_OF_DAY))}:${String.format("%02d", completeTimeDate.get(Calendar.MINUTE))}"
    }

    fun getOngoingDateTime(): String{
        return "${ongoingTimeDate.get(Calendar.YEAR)}/${ongoingTimeDate.get(Calendar.MONTH)+1}/${ongoingTimeDate.get(Calendar.DATE)} ${String.format("%02d", ongoingTimeDate.get(Calendar.HOUR_OF_DAY))}:${String.format("%02d", ongoingTimeDate.get(Calendar.MINUTE))}"
    }

    fun getDateOfMonth(): String{
        return "${completeTimeDate.get(Calendar.DATE)}"
    }

    fun getDayOfWeek(): String{

        return when(completeTimeDate.get(Calendar.DAY_OF_WEEK))
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
