package com.delivery.sopo.presentation.viewmodels.inquiry

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.data.repositories.local.repository.CarrierDataSource
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.TimeLineProgress
import com.delivery.sopo.domain.usecase.parcel.remote.UpdateParcelUseCase
import com.delivery.sopo.enums.SnackBarType
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.launch

class ParcelDetailViewModel(
                            private val updateParcelUseCase: UpdateParcelUseCase,
                            private val carrierDataSource: CarrierDataSource):
        BaseViewModel()
{
    // 상세 화면에서 사용할 데이터 객체
    private var _parcelDetail = MutableLiveData<Parcel.Detail>()
    val parcelDetail: LiveData<Parcel.Detail> = _parcelDetail

    // delivery status 리스트
    val statusList = MutableLiveData<MutableList<SelectItem<String>>?>()

    private var _navigator = MutableLiveData<String>()
    val navigator: LiveData<String> = _navigator

    private var _bottomSnackBar = MutableLiveData<SnackBarType>()
    val bottomSnackBar: LiveData<SnackBarType> = _bottomSnackBar

    fun postNavigator(navigator: String)
    {
        _navigator.postValue(navigator)
    }

    private suspend fun getParcelDetail(parcel: Parcel.Common): Parcel.Detail
    {
        val deliveryStatus = CodeUtil.getEnumValueOfName<DeliveryStatusEnum>(parcel.deliveryStatus)
        val carrier = carrierDataSource.getByCode(parcel.carrier)

        val progresses = parcel.trackingInfo?.progresses?.map { progress ->
            TimeLineProgress(date = progress?.getDate(), location = progress?.location?.name, description = progress?.description, status = progress?.status)
        } ?: emptyList()

        return Parcel.Detail(regDt = parcel.regDte, alias = parcel.alias, carrier = carrier, waybillNum = parcel.waybillNum, deliverStatus = deliveryStatus, timeLineProgresses = progresses.toMutableList())
    }

    // 택배의 이동 상태(indicator)의 값을 리스트 형식으로 반환 / true => 현재 상태
    fun getDeliveryStatusIndicator(deliveryStatus: DeliveryStatusEnum?): MutableList<SelectItem<String>>
    {
        val deliveryStatuses = enumValues<DeliveryStatusEnum>().map { status ->
            val isSelect = (deliveryStatus == status)
            SelectItem(item = status.TITLE, isSelect = isSelect)
        } as MutableList<SelectItem<String>>

        return deliveryStatuses
    }

    fun requestParcelDetail(parcelId: Int) = scope.launch{

        SopoLog.d("requestParcelDetail(...) parcel id = $parcelId")

        val localParcel = updateParcelUseCase.getLocalParcel(parcelId = parcelId)?: throw Exception("해당 택배를 찾을 수 없습니다.")
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

    override fun handlerAPIException(exception: SOPOApiException)
    {
        super.handlerAPIException(exception)
        if(exception.code == ErrorCode.VALIDATION) return

        _bottomSnackBar.postValue(SnackBarType.Error(exception.code.content, 3000))
//        postErrorSnackBar(exception.code.content)
    }

    override fun handlerInternalServerException(exception: InternalServerException)
    {
        super.handlerInternalServerException(exception)

        postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
    }

    override fun handlerException(exception: Exception)
    {
        super.handlerException(exception)
        postErrorSnackBar("[불명] ${exception.toString()}")
    }

}