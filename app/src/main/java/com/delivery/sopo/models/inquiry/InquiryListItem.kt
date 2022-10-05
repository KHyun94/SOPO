package com.delivery.sopo.models.inquiry

import androidx.databinding.ObservableField
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.enums.ParcelDepth
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.DateUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class InquiryListItem (val parcel: Parcel.Common, var isSelected: Boolean = false)
{
    val firstDepth: ParcelDepth.First
    get() = ParcelDepth.getParcelFirstDepth(parcel.deliveryStatus)

    private val ongoingAuditDte: Date? by lazy { DateUtil.convertDate(parcel.auditDte) }
    private val completedArrivalDte: Date? by lazy { parcel.arrivalDte?.let { DateUtil.convertDate(it) } }

    fun getCompleteYearMonth(): String
    {
        val calendar = Calendar.getInstance(Locale.KOREAN)
        calendar.time = completedArrivalDte ?: return "시간불명"
        return "${calendar.get(Calendar.YEAR)}/${calendar.get(Calendar.MONTH) + 1}"
    }

    fun getCompleteDateTime(): String
    {
        val now = System.currentTimeMillis()
        val target = ongoingAuditDte?.time ?: return "업데이트 일자 확인 불가"

        val diffMillis = (now - target)
        val diffHour = diffMillis / (1000 * 60 * 60)

        return when
        {
            diffHour < 1 -> "최근 1시간 내 업데이트"
            diffHour in 1..23 -> "${diffHour}시간 전 업데이트"
            diffHour in 24..743 ->
            {
                val diffDay = diffHour / 24
                "${diffDay}일 전 업데이트"
            }
            diffHour in 744..8927 ->
            {
                val diffMonth = diffHour / (24 * 31)
                "${diffMonth}개월 전 업데이트"
            }
            diffHour > 8927 ->
            {
                val diffYear = diffHour / (24 * 31 * 12)
                "${diffYear}년 전 업데이트"
            }
            else -> "업데이트 일자 확인 불가"
        }
    }

    fun getOngoingDateTime(): String
    {
        val milliseconds = ongoingAuditDte?.time ?: return "시간불명"
        return DateUtil.convertDate(milliseconds, DateUtil.DATE_TYPE_PROGRESSES) ?: "시간불명"
    }

    fun getDateOfMonth(): String
    {
        val calendar = Calendar.getInstance(Locale.KOREAN)
        calendar.time = completedArrivalDte ?: return ""

        return "${calendar.get(Calendar.DATE)}"
    }

    fun getDayOfWeek(): String
    {
        val calendar = Calendar.getInstance(Locale.KOREAN)
        calendar.time = completedArrivalDte ?: return ""

        return when(calendar.get(Calendar.DAY_OF_WEEK))
        {
            1 -> "일요일"
            2 -> "월요일"
            3 -> "화요일"
            4 -> "수요일"
            5 -> "목요일"
            6 -> "금요일"
            7 -> "토요일"
            else -> ""
        }
    }

    fun toCarrierName() = CarrierEnum.getCarrierByCode(parcel.carrier).NAME

    val isUnidentified = ObservableField<Boolean>().apply {
        checkIsUnidentified {
            set(it)
            notifyChange()
        }
    }

    fun checkIsUnidentified(cb: (Boolean) -> Unit) = CoroutineScope(Dispatchers.Main).launch {
//        val update: LiveData<Int?> = withContext(Dispatchers.Default) { parcelRepository.getIsUnidentifiedAsLiveData(parcel.parcelId) }
//
//        // TODO 이렇게 옵저빙안하고도 변경 가능한지 테스트 필시 해야함
//        update.observeForever {
//            cb.invoke(it != null && it == 1)
//        }
    }
}
