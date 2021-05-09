package com.delivery.sopo.models.inquiry

import androidx.databinding.BaseObservable
import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.R
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.data.repository.local.repository.ParcelRepoImpl
import kotlinx.coroutines.*
import org.koin.core.KoinComponent
import org.koin.core.inject
import java.text.SimpleDateFormat
import java.util.*

// TODO 추후 변경...
class InquiryListItem(val parcel: Parcel, var isSelected: Boolean = false): KoinComponent, BaseObservable()
{
    private val parcelRepoImpl: ParcelRepoImpl by inject()

    val iconResource = MutableLiveData<Int>().apply {
        postValue(getStatusBackgroundResource())
    }

    val backgroundColorResource = MutableLiveData<Int>().apply {
        postValue(getStatusBackgroundColorResource())
    }

    val statusText = MutableLiveData<String>().apply {
        postValue(getStatusText())
    }

    val statusTextColorResource = MutableLiveData<Int>().apply {
        postValue(getStatusTextColorResource())
    }

    val isUnidentified = ObservableField<Boolean>().apply {
        checkIsUnidentified {
            set(it)
            notifyChange()
        }
    }

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

    fun checkIsUnidentified(cb : (Boolean) -> Unit){

        CoroutineScope(Dispatchers.Main).launch {
            var update : LiveData<Int?>? = null

            withContext(Dispatchers.Default){
               update = parcelRepoImpl.getIsUnidentifiedAsLiveData(parcel.parcelId)
            }

            // TODO 이렇게 옵저빙안하고도 변경 가능한지 테스트 필시 해야함
            update?.observeForever{
                cb.invoke(it != null && it  == 1)
            }
        }
    }

    private fun getStatusText(): String
    {
        return when(parcel.deliveryStatus)
        {
            DeliveryStatusEnum.NOT_REGISTER.CODE -> "준비중"
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

    private fun getStatusTextColorResource(): Int
    {
        return when(parcel.deliveryStatus)
        {
            DeliveryStatusEnum.NOT_REGISTER.CODE -> R.color.COLOR_GRAY_300
            //상품 준비중
            DeliveryStatusEnum.INFORMATION_RECEIVED.CODE -> R.color.COLOR_GRAY_300
            //상품 인수
            DeliveryStatusEnum.AT_PICKUP.CODE -> R.color.COLOR_GRAY_300
            //상품 이동 중
            DeliveryStatusEnum.IN_TRANSIT.CODE -> R.color.MAIN_WHITE
            // 동네도착
            DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE -> R.color.MAIN_WHITE
            else -> R.color.COLOR_GRAY_300
        }
    }

    private fun getStatusBackgroundColorResource(): Int
    {
        return when(parcel.deliveryStatus)
        {
            DeliveryStatusEnum.NOT_REGISTER.CODE -> R.color.STATUS_PREPARING
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

    private fun getStatusBackgroundResource(): Int
    {
        return when(parcel.deliveryStatus)
        {
            DeliveryStatusEnum.NOT_REGISTER.CODE -> R.drawable.ic_inquiry_cardview_not_registered
            //상품 준비중
            DeliveryStatusEnum.INFORMATION_RECEIVED.CODE -> R.drawable.ic_inquiry_cardview_not_registered
            //상품 인수
            DeliveryStatusEnum.AT_PICKUP.CODE -> R.drawable.ic_inquiry_cardview_at_pickup
            //상품 이동 중
            DeliveryStatusEnum.IN_TRANSIT.CODE -> R.drawable.ic_inquiry_cardview_in_transit
            // 동네도착
            DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE -> R.drawable.ic_inquiry_cardview_out_for_delivery
            else -> 0
        }

    }

}
