package com.delivery.sopo.models.inquiry

import androidx.databinding.BaseObservable
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import com.delivery.sopo.R
import com.delivery.sopo.data.repositories.local.repository.ParcelRepository
import com.delivery.sopo.enums.CarrierEnum
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.DateUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.util.*

class InquiryListItem(var parcel: Parcel.Common, var isSelected: Boolean = false): KoinComponent, BaseObservable()
{
    private val parcelRepository: ParcelRepository by inject()

    val iconResource: Int by lazy { getParcelStatusIcon() }
    val backgroundColorResource: Int by lazy { getParcelStatusBackgroundColor() }
    val statusText: String by lazy { getParcelStatus() }
    val statusTextColorResource: Int by lazy { getParcelStatusColor() }

    val isUnidentified = ObservableField<Boolean>().apply {
        checkIsUnidentified {
            set(it)
            notifyChange()
        }
    }

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
        val target = completedArrivalDte?.time?: return "업데이트 일자 확인 불가"

        val diffMillis = (now - target)
        val diffHour = diffMillis / (1000 * 60 * 60)

        return when
        {
            diffHour < 1 ->
            {
                "최근 1시간 내 업데이트"
            }
            diffHour in 1..23 ->
            {
                "${diffHour}시간 전 업데이트"
            }
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
            else ->
            {
                "업데이트 일자 확인 불가"
            }
        }
    }

    fun getOngoingDateTime(): String
    {
        val milliseconds = ongoingAuditDte?.time ?: return "시간불명"
        return DateUtil.convertDate(milliseconds, DateUtil.DATE_TYPE_PROGRESSES)?:"시간불명"
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
            1 ->
            {
                "일요일"
            }
            2 ->
            {
                "월요일"
            }
            3 ->
            {
                "화요일"
            }
            4 ->
            {
                "수요일"
            }
            5 ->
            {
                "목요일"
            }
            6 ->
            {
                "금요일"
            }
            7 ->
            {
                "토요일"
            }
            else ->
            {
                ""
            }
        }
    }

    fun checkIsUnidentified(cb: (Boolean) -> Unit)
    {
        CoroutineScope(Dispatchers.Main).launch {
            var update: LiveData<Int?>? = null

            withContext(Dispatchers.Default) {
                update = parcelRepository.getIsUnidentifiedAsLiveData(parcel.parcelId)
            }

            // TODO 이렇게 옵저빙안하고도 변경 가능한지 테스트 필시 해야함
            update?.observeForever {
                cb.invoke(it != null && it == 1)
            }
        }
    }

    private fun getParcelStatus(): String
    {
        return when(parcel.deliveryStatus)
        {
            DeliveryStatusEnum.NOT_REGISTERED.CODE -> "준비중"
            DeliveryStatusEnum.ORPHANED.CODE -> "조회불가"
            //상품 준비중
            DeliveryStatusEnum.INFORMATION_RECEIVED.CODE -> "준비중"
            //상품 인수
            DeliveryStatusEnum.AT_PICKUP.CODE -> "상품인수"
            //상품 이동 중
            DeliveryStatusEnum.IN_TRANSIT.CODE -> "배송중"
            // 동네도착
            DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE -> "동네도착"
            else -> "에러"
        }
    }

    private fun getParcelStatusColor(): Int
    {
        return when(parcel.deliveryStatus)
        {
            DeliveryStatusEnum.NOT_REGISTERED.CODE -> R.color.COLOR_GRAY_300
            DeliveryStatusEnum.ORPHANED.CODE -> R.color.COLOR_MAIN_300
            //상품 준비중
            DeliveryStatusEnum.INFORMATION_RECEIVED.CODE -> R.color.COLOR_GRAY_300
            //상품 인수
            DeliveryStatusEnum.AT_PICKUP.CODE -> R.color.COLOR_MAIN_300
            // 배송
            DeliveryStatusEnum.IN_TRANSIT.CODE -> R.color.MAIN_WHITE
            // 동네도착
            DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE -> R.color.MAIN_WHITE
            else -> R.color.COLOR_GRAY_300
        }
    }

    private fun getParcelStatusBackgroundColor(): Int
    {
        return when(parcel.deliveryStatus)
        {
            DeliveryStatusEnum.NOT_REGISTERED.CODE -> R.color.STATUS_PREPARING
            DeliveryStatusEnum.ORPHANED.CODE -> R.color.MAIN_WHITE
            DeliveryStatusEnum.INFORMATION_RECEIVED.CODE -> R.color.STATUS_PREPARING
            DeliveryStatusEnum.AT_PICKUP.CODE -> R.color.STATUS_PREPARING
            DeliveryStatusEnum.IN_TRANSIT.CODE -> R.color.STATUS_ING
            DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE -> R.color.COLOR_MAIN_700
            else -> R.color.STATUS_PREPARING
        }
    }

    private fun getParcelStatusIcon(): Int
    {
        return when(parcel.deliveryStatus)
        {
            DeliveryStatusEnum.NOT_REGISTERED.CODE -> R.drawable.ic_inquiry_cardview_not_registered
            DeliveryStatusEnum.ORPHANED.CODE -> R.drawable.ic_inquiry_cardview_orphaned
            DeliveryStatusEnum.INFORMATION_RECEIVED.CODE -> R.drawable.ic_inquiry_cardview_not_registered
            DeliveryStatusEnum.AT_PICKUP.CODE -> R.drawable.ic_inquiry_cardview_at_pickup
            DeliveryStatusEnum.IN_TRANSIT.CODE -> R.drawable.ic_inquiry_cardview_in_transit_test
            DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE -> R.drawable.ic_inquiry_cardview_out_for_delivery
            else -> R.drawable.ic_inquiry_cardview_error
        }
    }

    fun toCarrierName() = CarrierEnum.getCarrierByCode(parcel.carrier).NAME
}
