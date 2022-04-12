package com.delivery.sopo.models.inquiry

import androidx.databinding.BaseObservable
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import com.delivery.sopo.R
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.SopoLog
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
            set(it && !parcel.reported)
            notifyChange()
        }
    }

    private val ongoingTimeDate: Calendar? by lazy {

        SopoLog.d("Inquiry Test ${parcel.toString()}")

        if(parcel.auditDte == "")
        {
            SopoLog.d("에러")
            return@lazy null
        }

        val time = DateUtil.changeDateFormat2(parcel.auditDte.replace("T", " "))?:return@lazy null
        val calendar = Calendar.getInstance().apply {
            this.time = time
        }
        return@lazy calendar
    }

    private val completeTimeDate: Calendar? by lazy {
        val time = DateUtil.changeDateFormat2(parcel.arrivalDte?.replace("T", " ")?:"")?:return@lazy null
        val calendar = Calendar.getInstance().apply {
            this.time = time
        }
        return@lazy calendar
    }


    fun getCompleteYearMonth(): String
    {
        if(completeTimeDate == null) return "시간불명"
        return DateUtil.changeCalendarToDate(completeTimeDate!!)
    }

    fun getCompleteDateTime(): String
    {
        if(completeTimeDate == null) return "시간불명"
        return DateUtil.changeCalendarToDateTime(completeTimeDate!!)
    }

    fun getOngoingDateTime(): String
    {
        if(ongoingTimeDate == null) return "시간불명"
        return DateUtil.changeCalendarToDateTime(ongoingTimeDate!!)
    }

    fun getDateOfMonth(): String
    {
        return "${completeTimeDate?.get(Calendar.DATE)}"
    }

    fun getDayOfWeek(): String
    {

        return when(completeTimeDate?.get(Calendar.DAY_OF_WEEK))
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
            //상품 준비중
            DeliveryStatusEnum.INFORMATION_RECEIVED.CODE -> R.color.STATUS_PREPARING
            //상품 인수
            DeliveryStatusEnum.AT_PICKUP.CODE -> R.color.STATUS_PREPARING
            //상품 이동 중
            DeliveryStatusEnum.IN_TRANSIT.CODE -> R.color.STATUS_ING
            // 동네도착
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
            //상품 준비중
            DeliveryStatusEnum.INFORMATION_RECEIVED.CODE -> R.drawable.ic_inquiry_cardview_not_registered
            //상품 인수
            //            DeliveryStatusEnum.AT_PICKUP.CODE -> R.drawable.ic_inquiry_cardview_at_pickup
            DeliveryStatusEnum.AT_PICKUP.CODE -> R.drawable.ic_inquiry_cardview_at_pickup_jpg
            //상품 이동 중
            //            DeliveryStatusEnum.IN_TRANSIT.CODE -> R.drawable.ic_inquiry_cardview_in_transit
            DeliveryStatusEnum.IN_TRANSIT.CODE -> R.drawable.ic_inquiry_cardview_in_transit_test
            // 동네도착
            DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE -> R.drawable.ic_inquiry_cardview_out_for_delivery
            else -> R.drawable.ic_inquiry_cardview_error
        }

    }

    fun toParcelString(){
        SopoLog.d("parcel:${parcel.toString()}")
    }
}
