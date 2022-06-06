package com.delivery.sopo.presentation.viewmodels.inquiry

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.data.repositories.local.repository.CarrierRepository
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.TimeLineProgress
import com.delivery.sopo.domain.usecase.parcel.remote.UpdateParcelUseCase
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.launch

class ParcelDetailViewModel(
                            private val updateParcelUseCase: UpdateParcelUseCase,
                            private val carrierRepository: CarrierRepository):
        BaseViewModel()
{
    // 상세 화면에서 사용할 데이터 객체
    private var _parcelDetail = MutableLiveData<Parcel.Detail>()
    val parcelDetail: LiveData<Parcel.Detail>
        get() = _parcelDetail

    // delivery status 리스트
    val statusList = MutableLiveData<MutableList<SelectItem<String>>?>()

    private var _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    fun postNavigator(navigator: String)
    {
        _navigator.postValue(navigator)
    }

    private suspend fun getParcelDetail(parcel: Parcel.Common): Parcel.Detail
    {
        val deliveryStatus = CodeUtil.getEnumValueOfName<DeliveryStatusEnum>(parcel.deliveryStatus)
        val carrier = carrierRepository.getByCode(parcel.carrier)

        val progresses = parcel.trackingInfo?.progresses?.map { progress ->
            TimeLineProgress(date = progress?.getDate(), location = progress?.location?.name, description = progress?.description, status = progress?.status)
        } ?: emptyList()

        return Parcel.Detail(regDt = parcel.regDte, alias = parcel.alias, carrier = carrier, waybillNum = parcel.waybillNum, deliverStatus = deliveryStatus, timeLineProgresses = progresses.toMutableList())
    }

    // 택배의 이동 상태(indicator)의 값을 리스트 형식으로 반환 / true => 현재 상태
    fun getDeliveryStatusIndicator(deliveryStatus: DeliveryStatusEnum?): MutableList<SelectItem<String>>
    {
        val deliveryStatuses = enumValues<DeliveryStatusEnum>().map { status ->
            val isSelect = deliveryStatus == status
            SelectItem(item = status.TITLE, isSelect = isSelect)
        } as MutableList<SelectItem<String>>

        return deliveryStatuses
    }

    fun requestParcelDetail(parcelId: Int) = scope.launch{

        SopoLog.d("requestParcelDetail(...) parcel id = $parcelId")

        val localParcel = updateParcelUseCase.getLocalParcel(parcelId = parcelId)?: throw Exception("이 시발 여기냐 섭라")
        SopoLog.d("Local Parcel $localParcel")
        val localParcelDetail = getParcelDetail(localParcel)
        _parcelDetail.postValue(localParcelDetail)

        if(localParcelDetail.deliverStatus == DeliveryStatusEnum.DELIVERED) return@launch

        val remoteParcel = updateParcelUseCase(parcelId = parcelId)
        SopoLog.d("Remote Parcel $remoteParcel")
        if(localParcel.inquiryHash == remoteParcel.inquiryHash) return@launch

        val remoteParcelDetail = getParcelDetail(localParcel)
        _parcelDetail.postValue(remoteParcelDetail)
    }

    fun onBackClicked()
    {
        postNavigator(NavigatorConst.Event.BACK)
    }

    fun onDownClicked(): View.OnClickListener
    {
        return View.OnClickListener() {
            postNavigator(NavigatorConst.Event.BACK)
        }
    }

    override var onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onFailure(error: ErrorCode)
        {
            postErrorSnackBar("알 수 없는 이유로 등록에 실패했습니다.[${error.toString()}]")
        }

        override fun onInternalServerError(error: ErrorCode)
        {
            super.onInternalServerError(error)

            postErrorSnackBar("일시적으로 서비스를 이용할 수 없습니다.[${error.toString()}]")
        }

        override fun onAuthError(error: ErrorCode)
        {
            super.onAuthError(error)

            postErrorSnackBar("유저 인증에 실패했습니다. 다시 시도해주세요.[${error.toString()}]")
        }
    }
}