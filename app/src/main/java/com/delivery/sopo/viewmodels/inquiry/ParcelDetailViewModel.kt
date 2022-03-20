package com.delivery.sopo.viewmodels.inquiry

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.consts.DeliveryStatusConst
import com.delivery.sopo.consts.NavigatorConst
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.database.room.entity.ParcelEntity
import com.delivery.sopo.data.repository.local.repository.CarrierRepository
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.TimeLineProgress
import com.delivery.sopo.networks.call.ParcelCall
import com.delivery.sopo.usecase.parcel.local.GetLocalParcelUseCase
import com.delivery.sopo.util.CodeUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.livedates.SingleLiveEvent
import com.delivery.sopo.views.adapter.TimeLineRecyclerViewAdapter
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ParcelDetailViewModel(private val getLocalParcelUseCase: GetLocalParcelUseCase, private val carrierRepository: CarrierRepository, private val parcelRepo: ParcelRepository, private val parcelManagementRepoImpl: ParcelManagementRepoImpl):
        BaseViewModel()
{
    // 상세 화면에서 사용할 데이터 객체
    val parcelDetail = MutableLiveData<Parcel.Detail>()

    // delivery status 리스트
    val statusList = MutableLiveData<MutableList<SelectItem<String>>?>()

    private var _navigator = MutableLiveData<String>()
    val navigator : LiveData<String>
        get() = _navigator

    fun setNavigator(navigator: String){
        _navigator.postValue(navigator)
    }

    private suspend fun getParcelDetail(parcel: Parcel.Common): Parcel.Detail
    {
        val deliveryStatus = CodeUtil.getEnumValueOfName<DeliveryStatusEnum>(parcel.deliveryStatus)
        val carrier = carrierRepository.getCarrierWithCode(parcel.carrier)

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

    // remote data를 요청과 동시에
    suspend fun requestParcelDetailData(parcelId: Int)
    {
        SopoLog.d("requestRemoteParcel() 호출 - parcelId[$parcelId]")
        withContext(Dispatchers.Default) { requestLocalParcel(parcelId) }
        withContext(Dispatchers.IO) { requestParcelForRefresh(parcelId = parcelId) }

        updateUnidentifiedStatusToZero(parcelId = parcelId)
    }

    // 로컬에 저장된 택배 인포를 로드
    private suspend fun requestLocalParcel(parcelId: Int)
    {
        SopoLog.d("requestLocalParcel() 호출 - parcelId[${parcelId}]")

        val parcel = getLocalParcelUseCase.invoke(parcelId = parcelId) ?: return
        val parcelDetail = getParcelDetail(parcel = parcel)

        this.parcelDetail.postValue(parcelDetail)
    }

    suspend fun requestParcelForRefresh(parcelId: Int)
    {
        SopoLog.d("requestParcelForRefresh() 호출 - parcelId[$parcelId]")
        val res = ParcelCall.requestParcelForRefresh(parcelId = parcelId)
    }

    /*fun getRemoteParcel(parcelId: Int) = scope.launch(Dispatchers.IO) {
        SopoLog.i("getRemoteParcel(...) 호출 [택배 번호:${parcelId.toString()}]")

        try
        {
            val parcel = parcelRepo.getRemoteParcelById(parcelId = parcelId)

            val parcelDetail = getParcelDetail(parcel = parcel)

            this@ParcelDetailViewModel.parcelDetail.postValue(parcelDetail)

//            statusList.postValue(parcelDetail.deliverStatus?.CODE?.let { getDeliveryStatusIndicator(deliveryStatus = it) })

            val parcelEntity = ParcelMapper.parcelToParcelEntity(parcel)

            updateParcelData(parcelEntity = parcelEntity)
            updateIsBeUpdate(parcelId = parcelId, status = StatusConst.DEACTIVATE)
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }

    }*/

    private suspend fun updateParcelData(parcelEntity: ParcelEntity) =
        withContext(Dispatchers.Default) {
            parcelRepo.update(parcelEntity)
        }

    private suspend fun updateIsBeUpdate(parcelId: Int, status: Int) =
        withContext(Dispatchers.Default) {
            parcelManagementRepoImpl.updateUpdatableStatus(parcelId, status)
        }

    fun onBackClicked()
    {
        setNavigator(NavigatorConst.TO_BACK_SCREEN)
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
            setNavigator(NavigatorConst.TO_BACK_SCREEN)
        }
    }

    override val exceptionHandler: CoroutineExceptionHandler
        get() = TODO("Not yet implemented")
}