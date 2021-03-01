package com.delivery.sopo.models.inquiry

import androidx.lifecycle.LiveData
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

class InquiryListItem(
    val parcel: Parcel,
    var isSelected: Boolean = false,
    var isUnidentified : Boolean = true
){
    private val completeTimeDate: Calendar by lazy {
        Calendar.getInstance().apply { this.time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(parcel.arrivalDte?.replace("T", " ")) }
    }
    private val ongoingTimeDate: Calendar by lazy {
        Calendar.getInstance().apply { this.time = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA).parse(parcel.auditDte.replace("T", " ")) }
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

    fun setUpdateValue(parcelRepoImpl: ParcelRepoImpl, cb : (Boolean?) -> Unit){

        CoroutineScope(Dispatchers.Main).launch {
            var update : LiveData<Int?>? = null
            withContext(Dispatchers.Default){
               update = parcelRepoImpl.getIsUnidentifiedLiveData(parcel.parcelId.regDt, parcel.parcelId.parcelUid)
            }

            update?.observeForever{
                SopoLog.d(msg = "[${parcel.parcelAlias}] => $it")

                isUnidentified = it != null && it  == 1

                cb.invoke(isUnidentified)
            }
        }
    }


}
