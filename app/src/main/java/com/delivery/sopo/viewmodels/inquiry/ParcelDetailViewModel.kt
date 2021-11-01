package com.delivery.sopo.viewmodels.inquiry

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.consts.DeliveryStatusConst
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.repository.database.room.entity.ParcelEntity
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.parcel.*
import com.delivery.sopo.networks.call.ParcelCall
import com.delivery.sopo.data.repository.local.repository.CarrierRepository
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.enums.DisplayEnum
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.livedates.SingleLiveEvent
import com.delivery.sopo.views.adapter.TimeLineRvAdapter
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.lang.NullPointerException

class ParcelDetailViewModel(private val carrierRepository: CarrierRepository, private val parcelRepo: ParcelRepository, private val parcelManagementRepoImpl: ParcelManagementRepoImpl):
        BaseViewModel()
{
    // 택배 인포의 pk
    val parcelId = MutableLiveData<Int>()

    // delivery status 리스트
    val statusList = MutableLiveData<MutableList<SelectItem<String>>?>()
    var adapter = MutableLiveData<TimeLineRvAdapter?>()

    // 상세 화면에서 사용할 데이터 객체
    var item = MutableLiveData<ParcelDetailDTO?>()

    // 상세 페이지 택배 상태(백그라운드 이미지, 텍스트)
    var deliveryStatus = MutableLiveData<DeliveryStatusEnum?>()

    // 상세 화면 종료
    var isBack = SingleLiveEvent<Boolean>()

    // 상세 화면 Full Down
    var isDragOut = SingleLiveEvent<Boolean>()

    // 업데이트 여부
    private val _updateType = MutableLiveData<Int>()
    val updateType: LiveData<Int>
        get() = _updateType

    private val _isProgress = MutableLiveData<Boolean>()
    val isProgress: LiveData<Boolean>
        get() = _isProgress

    private val _result = MutableLiveData<ResponseResult<*>>()
    val result: LiveData<ResponseResult<*>>
        get() = _result

    // Full Detail View의 리사이클러뷰 어댑터 세팅
    private fun getTimeLineRvAdapter(list: List<TimeLineProgress?>): TimeLineRvAdapter
    {
        SopoLog.d("getTimeLineRvAdapter() 호출 - list[${list.size}]")
        return TimeLineRvAdapter().apply { setItemList(list.toMutableList()) }
    }

    // 택배 상세 UI 세팅
    private suspend fun updateParcelToUI(parcelDTO: ParcelDTO)
    {
        SopoLog.d("updateParcelToUI() 호출")

        val progressList = mutableListOf<TimeLineProgress>()

        val deliveryStatus = DeliveryStatusConst.getDeliveryStatus(parcelDTO.deliveryStatus)

        deliveryStatus.let { enum ->
            this.deliveryStatus.postValue(enum)
            statusList.postValue(getDeliveryStatusIndicator(deliveryStatus = enum.CODE))
        }

        // ParcelEntity의 택배사 코드를 이용하여 택배사 정보를 로컬 DB에서 읽어온다.
        val carrierDTO = carrierRepository.getCarrierWithCode(parcelDTO.carrier)

        if(parcelDTO.inquiryResult != null && parcelDTO.inquiryResult != "")
        {
            val parcelItem: ParcelItem = Gson().fromJson<ParcelItem>(parcelDTO.inquiryResult, ParcelItem::class.java)

            parcelItem.progresses.forEach { progressess ->
                val date = progressess?.getDate()
                val progress = TimeLineProgress(date = date, location = progressess?.location?.name,
                                                description = progressess?.description,
                                                status = progressess?.status)
                progressList.add(progress)
            }
        }

        val parcelDetailDTO = ParcelDetailDTO(regDt = parcelDTO.regDt, alias = parcelDTO.alias,
                                              carrierDTO = carrierDTO,
                                              waybillNum = parcelDTO.waybillNum,
                                              deliverStatus = this.deliveryStatus.value?.TITLE,
                                              timeLineProgresses = progressList)

        item.postValue(parcelDetailDTO)
        adapter.postValue(getTimeLineRvAdapter(progressList))
    }

    // 택배의 이동 상태(indicator)의 값을 리스트 형식으로 반환 / true => 현재 상태
    private fun getDeliveryStatusIndicator(deliveryStatus: String): MutableList<SelectItem<String>>
    {
        val statusList = mutableListOf(SelectItem(DeliveryStatusEnum.AT_PICKUP.TITLE, false),
                                       SelectItem(DeliveryStatusEnum.IN_TRANSIT.TITLE, false),
                                       SelectItem(DeliveryStatusEnum.OUT_FOR_DELIVERY.TITLE, false),
                                       SelectItem(DeliveryStatusEnum.DELIVERED.TITLE, false))

        when(deliveryStatus)
        {
            DeliveryStatusConst.INFORMATION_RECEIVED -> statusList[0].isSelect = true
            DeliveryStatusConst.AT_PICKUP -> statusList[0].isSelect = true
            DeliveryStatusConst.IN_TRANSIT -> statusList[1].isSelect = true
            DeliveryStatusConst.OUT_FOR_DELIVERY -> statusList[2].isSelect = true
            DeliveryStatusConst.DELIVERED -> statusList[3].isSelect = true
        }

        return statusList
    }

    // remote data를 요청과 동시에
    suspend fun requestParcelDetailData(parcelId: Int)
    {
        SopoLog.d("requestRemoteParcel() 호출 - parcelId[$parcelId]")
        withContext(Dispatchers.Default) { requestLocalParcel(parcelId) }
        withContext(Dispatchers.IO) { requestParcelForRefresh(parcelId = parcelId) }
    }

    // 로컬에 저장된 택배 인포를 로드
    private suspend fun requestLocalParcel(parcelId: Int)
    {
        SopoLog.d("requestLocalParcel() 호출 - parcelId[${parcelId}]")

        val parcelEntity =
            parcelRepo.getLocalParcelById(parcelId = parcelId) ?: throw NullPointerException(
                "내부에 저장된 parcelId[$parcelId]의 상세 내역이 존재하지 않습니다.")
        val parcelDTO = ParcelMapper.parcelEntityToParcel(parcelEntity = parcelEntity)

        updateParcelToUI(parcelDTO)
    }

    suspend fun requestParcelForRefresh(parcelId: Int)
    {
        SopoLog.d("requestParcelForRefresh() 호출 - parcelId[$parcelId]")
        val res = ParcelCall.requestParcelForRefresh(parcelId = parcelId)

        if(!res.result)
        {
            SopoLog.d("업데이트 여부 체크 ${res.code} / ${res}")
            when(res.code)
            {
                ResponseCode.PARCEL_NOTHING_TO_UPDATES ->
                {
                    SopoLog.d("상세 내역 업데이트할 요소가 없습니다.")
                }
                ResponseCode.PARCEL_SOMETHING_TO_UPDATES ->
                {
                    SopoLog.d("상세 내역 업데이트할 요소가 있습니다.")
                    _updateType.postValue(StatusConst.SUCCESS)
                }
                else ->
                {
                    SopoLog.e("상세 내역 업데이트 조회 실패 message:[${res.message}], code:[${res.code}]")
                    _updateType.postValue(StatusConst.FAILURE)
                }
            }
        }
    }

    fun getRemoteParcel(parcelId: Int) = scope.launch(Dispatchers.IO) {
        SopoLog.i("getRemoteParcel(...) 호출 [택배 번호:${parcelId.toString()}]")

        try
        {
            val result = parcelRepo.getRemoteParcelById(parcelId = parcelId)

            val parcelDTO = result

            updateParcelToUI(parcelDTO)

            val parcelEntity = ParcelMapper.parcelToParcelEntity(parcelDTO)

            updateParcelData(parcelEntity = parcelEntity)
            updateIsBeUpdate(parcelId = parcelId, status = StatusConst.DEACTIVATE)
        }catch(e:Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }

    }

    private suspend fun updateParcelData(parcelEntity: ParcelEntity) =
        withContext(Dispatchers.Default) {
            parcelRepo.updateEntity(parcelEntity)
        }

    private suspend fun updateIsBeUpdate(parcelId: Int, status: Int) =
        withContext(Dispatchers.Default) {
            parcelManagementRepoImpl.updateUpdatableStatus(parcelId, status)
        }

    fun onBackClicked()
    {
        isBack.value = true
    }

    /**
     * isUnidentified 를 Activate -> Deactivate로 수정
     * unidentified => 내부 DB에 업데이트는 되어있으나, 사용자가 확인하지 않은 상태
     * */
    suspend fun updateUnidentifiedStatusToZero(parcelId: Int) = withContext(Dispatchers.Default) {
        parcelManagementRepoImpl.run {
            val status = getUnidentifiedStatusByParcelId(parcelId)
            if(status == StatusConst.ACTIVATE) parcelManagementRepoImpl.updateIsUnidentified(
                parcelId = parcelId, value = StatusConst.DEACTIVATE)
        }
    }

    fun onDownClicked(): View.OnClickListener
    {
        return View.OnClickListener() {
            isDragOut.value = true
        }
    }

    override val exceptionHandler: CoroutineExceptionHandler
        get() = TODO("Not yet implemented")
}