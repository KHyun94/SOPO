package com.delivery.sopo.viewmodels.inquiry

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.repository.local.repository.CarrierRepository
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.exceptions.ParcelExceptionHandler
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.TimeLineProgress
import com.delivery.sopo.usecase.parcel.local.GetLocalParcelUseCase
import com.delivery.sopo.usecase.parcel.remote.RefreshParcelUseCase
import com.delivery.sopo.util.CodeUtil
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ParcelDetailViewModel(private val getLocalParcelUseCase: GetLocalParcelUseCase, private val refreshParcelUseCase: RefreshParcelUseCase, private val carrierRepository: CarrierRepository, private val parcelRepo: ParcelRepository, private val parcelManagementRepoImpl: ParcelManagementRepoImpl):
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

    fun requestParcelDetail(parcelId: Int) = scope.launch(coroutineExceptionHandler) {

        updateUnidentifiedStatusToZero(parcelId = parcelId)
        updateIsBeUpdate(parcelId = parcelId, status = StatusConst.DEACTIVATE)

        requestLocalParcel(parcelId)
        requestRemoteParcel(parcelId = parcelId)
    }

    // 로컬에 저장된 택배 인포를 로드
    private suspend fun requestLocalParcel(parcelId: Int) = withContext(coroutineExceptionHandler) {
        val parcel = getLocalParcelUseCase.invoke(parcelId = parcelId) ?: return@withContext
        val parcelDetail = getParcelDetail(parcel = parcel)

        _parcelDetail.postValue(parcelDetail)

        if(parcelDetail.deliverStatus == DeliveryStatusEnum.DELIVERED) return@withContext

        val remoteParcel = refreshParcelUseCase.invoke(parcelId = parcelId)
        val remoteParcelDetail = getParcelDetail(remoteParcel)

        _parcelDetail.postValue(remoteParcelDetail)
    }

    suspend fun requestRemoteParcel(parcelId: Int) = withContext(Dispatchers.IO) {


    }

    private suspend fun updateIsBeUpdate(parcelId: Int, status: Int) =
        withContext(Dispatchers.Default) {
            parcelManagementRepoImpl.updateUpdatableStatus(parcelId, status)
        }

    fun onBackClicked()
    {
        postNavigator(NavigatorConst.TO_BACK_SCREEN)
    }

    /**
     * isUnidentified 를 Activate -> Deactivate로 수정
     * unidentified => 내부 DB에 업데이트는 되어있으나, 사용자가 확인하지 않은 상태
     * */
    suspend fun updateUnidentifiedStatusToZero(parcelId: Int) = withContext(Dispatchers.Default) {
        parcelManagementRepoImpl.run {
            val status = getUnidentifiedStatusByParcelId(parcelId)
            if(status == StatusConst.ACTIVATE) parcelManagementRepoImpl.updateUnidentifiedStatus(parcelId = parcelId, value = StatusConst.DEACTIVATE)
        }
    }

    fun onDownClicked(): View.OnClickListener
    {
        return View.OnClickListener() {
            postNavigator(NavigatorConst.TO_BACK_SCREEN)
        }
    }

    override var onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onFailure(error: ErrorEnum)
        {
            postErrorSnackBar("알 수 없는 이유로 등록에 실패했습니다.[${error.toString()}]")
        }

        override fun onInternalServerError(error: ErrorEnum)
        {
            super.onInternalServerError(error)

            postErrorSnackBar("일시적으로 서비스를 이용할 수 없습니다.[${error.toString()}]")
        }

        override fun onAuthError(error: ErrorEnum)
        {
            super.onAuthError(error)

            postErrorSnackBar("유저 인증에 실패했습니다. 다시 시도해주세요.[${error.toString()}]")
        }
    }
}