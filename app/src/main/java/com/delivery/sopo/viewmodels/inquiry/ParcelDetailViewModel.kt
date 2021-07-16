package com.delivery.sopo.viewmodels.inquiry

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.consts.DeliveryStatusConst
import com.delivery.sopo.data.repository.database.room.entity.ParcelEntity
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.consts.ResultTypeConst
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.TestResult
import com.delivery.sopo.models.parcel.*
import com.delivery.sopo.networks.call.ParcelCall
import com.delivery.sopo.data.repository.local.repository.CarrierRepository
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.livedates.SingleLiveEvent
import com.delivery.sopo.views.adapter.TimeLineRvAdapter
import com.google.gson.Gson
import kotlinx.coroutines.*

class ParcelDetailViewModel(private val userLocalRepository: UserLocalRepository, private val carrierRepository: CarrierRepository, private val parcelRepository: ParcelRepository, private val parcelManagementRepoImpl: ParcelManagementRepoImpl): ViewModel()
{
    // 택배 인포의 pk
    val parcelId = MutableLiveData<Int>()

    // delivery status 리스트
    val statusList = MutableLiveData<MutableList<SelectItem<String>>?>()
    var adapter = MutableLiveData<TimeLineRvAdapter?>()

    private val _parcelEntity = MutableLiveData<ParcelEntity>()
    val parcelEntity: LiveData<ParcelEntity>
        get() = _parcelEntity

    // parcelEntity 중 inquiryResult를 객체화시키는 용도
    var parcelItem: ParcelItem? = null

    // 상세 화면에서 사용할 데이터 객체
    var item = MutableLiveData<ParcelDetailItem?>()

    // 상세 페이지 택배 상태(백그라운드 이미지, 텍스트)
    var deliveryStatusEnum = MutableLiveData<DeliveryStatusEnum?>()

    // 상세 화면 종료
    var isBack = SingleLiveEvent<Boolean>()

    // 상세 화면 Full Down
    var isDown = SingleLiveEvent<Boolean>()

    // 업데이트 여부
    private val _isUpdate = MutableLiveData<Int>()
    val isUpdate: LiveData<Int>
        get() = _isUpdate

    private val _isProgress = MutableLiveData<Boolean>()
    val isProgress: LiveData<Boolean>
        get() = _isProgress

    private val _result = MutableLiveData<TestResult>()
    val result: LiveData<TestResult>
        get() = _result

    // Full Detail View의 리사이클러뷰 어댑터 세팅
    private fun setAdapter(list: List<Progress?>): TimeLineRvAdapter
    {
        SopoLog.d("setAdapter() call")
        return TimeLineRvAdapter().apply { setItemList(list.toMutableList()) }
    }

    // 택배 상세 UI 세팅
    fun updateParcelToUI(parcelEntity: ParcelEntity)
    {
        SopoLog.d("updateParcelToUI(${parcelEntity}) call")

        val progressList = mutableListOf<Progress>()

        CoroutineScope(Dispatchers.Main).launch {

            withContext(Dispatchers.IO) {
                // inquiryResult 객체
                parcelItem =
                    Gson().fromJson<ParcelItem?>(parcelEntity.inquiryResult, ParcelItem::class.java)
                        .also {
                            SopoLog.d(msg = "inquiryResult >>> ${it.toString()}")
                        }
            }

            withContext(Dispatchers.IO) {
                // Delivery Status

                DeliveryStatusConst.getDeliveryStatus(parcelEntity.deliveryStatus).let { enum ->
                    deliveryStatusEnum.postValue(enum)
                    statusList.postValue(getDeliveryStatusIndicator(deliveryStatus = enum.CODE))
                }
            }

            withContext(Dispatchers.IO) {

                // ParcelEntity의 택배사 코드를 이용하여 택배사 정보를 로컬 DB에서 읽어온다.
                val carrierDTO = runBlocking(Dispatchers.Default) { carrierRepository.getCarrierWithCode(parcelEntity.carrier) }

                SopoLog.d("택배사 정보 >>> $carrierDTO")

                if (progressList.size > 0) progressList.clear()

                // 프로그레스(택배의 경로 내용이 있을 때 RecyclerView 없을 땐 텍스트로 없다고 표시)

                parcelItem?.progresses?.forEach { item ->
                    val dateList = DateUtil.changeDateFormat(item!!.time!!)!!.split(" ")
                    val date = Date(dateList[0], dateList[1])
                    val progress = Progress(date = date, location = item.location!!.name, description = item.description, status = item.status)
                    progressList.add(progress)
                }

                SopoLog.d("""
                    ParcelDetailItem >>>
                    parcelId = ${parcelEntity.parcelId}, 
                    alias = ${parcelEntity.alias}, 
                    carrier = ${carrierDTO!!}, 
                    waybillNum = ${parcelEntity.waybillNum}, 
                    deliverStatus = ${deliveryStatusEnum.value?.TITLE}, 
                    progress = ${progressList.joinToString()}
                """.trimIndent())

                item.postValue(ParcelDetailItem(regDt = parcelEntity.regDt, alias = parcelEntity.alias, carrierDTO = carrierDTO, waybillNum = parcelEntity.waybillNum, deliverStatus = deliveryStatusEnum.value?.TITLE, progress = progressList))
            }

            SopoLog.d(msg = "Detail Item => ${item.value}")

            adapter.postValue(setAdapter(progressList))
        }
    }

    // 택배의 이동 상태(indicator)의 값을 리스트 형식으로 반환 / true => 현재 상태
    private fun getDeliveryStatusIndicator(deliveryStatus: String): MutableList<SelectItem<String>>
    {
        SopoLog.d("!!!!!!!!!!!!!!!>>>$deliveryStatus")

        val statusList = mutableListOf<SelectItem<String>>(
            SelectItem(DeliveryStatusEnum.AT_PICKUP.TITLE, false),
            SelectItem(DeliveryStatusEnum.IN_TRANSIT.TITLE, false),
            SelectItem(DeliveryStatusEnum.OUT_FOR_DELIVERY.TITLE, false),
            SelectItem(DeliveryStatusEnum.DELIVERED.TITLE, false)
        )

        when (deliveryStatus)
        {
            DeliveryStatusConst.INFORMATION_RECEIVED -> statusList[0].isSelect = true
            DeliveryStatusConst.AT_PICKUP -> statusList[0].isSelect = true
            DeliveryStatusConst.IN_TRANSIT -> statusList[1].isSelect = true
            DeliveryStatusConst.OUT_FOR_DELIVERY -> statusList[2].isSelect = true
            DeliveryStatusConst.DELIVERED -> statusList[3].isSelect = true
        }

        return statusList
    }

    // 로컬에 저장된 택배 인포를 로드
    private fun requestLocalParcel(parcelId:Int)
    {
        SopoLog.d("requestLocalParcel(${parcelId}) call")

        CoroutineScope(Dispatchers.Default).launch {

            // 로컬 데이터 옵저빙
            _parcelEntity.postValue(parcelRepository.getLocalParcelById(parcelId = parcelId))
        }
    }

    // remote data를 요청과 동시에
    fun requestRemoteParcel(parcelId:Int)
    {
        SopoLog.d("requestRemoteParcel(${parcelId}) call")


        requestLocalParcel(parcelId)

        // remote data를 갱신하도록 서버에 요
        CoroutineScope(Dispatchers.IO).launch {
            when (val result = ParcelCall.requestParcelForRefresh(parcelId))
            {
                is NetworkResult.Success ->
                {
                    SopoLog.d("success to requestRemoteParcel()")
                }
                is NetworkResult.Error ->
                {
                    val error = result.exception as APIException
                    SopoLog.e(msg = "(${result.statusCode})택배 상세 내역 에러 => (${error.responseCode}) ${error.responseCode}", e = error)

                    // TODO SUCCESS로 이동 204 303 등은 body가 null이어서 현재는 에러로 빠짐
                    when (result.statusCode)
                    {
                        204 ->
                        {
                            // 업데이트 할게 없음
                            SopoLog.d("상세 내역 업데이트할 요소가 없습니다.")
                        }
                        303 ->
                        {
                            SopoLog.d("상세 내역 업데이트할 요소가있습니다.")
                            _isUpdate.postValue(StatusConst.SUCCESS)
                        }
                        else ->
                        {
                            _isUpdate.postValue(StatusConst.FAILURE)
                            SopoLog.e("${error.errorMessage}", error)
                        }
                    }
                }
            }

        }


    }

    fun getRemoteParcel(parcelId:Int)
    {
        SopoLog.d("getRemoteParcel() call >>> ${parcelId.toString()}")

        CoroutineScope(Dispatchers.IO).launch {
            when (val result = ParcelCall.getSingleParcel(parcelId = parcelId))
            {
                is NetworkResult.Success ->
                {
                    val apiResult = result.data
                    val parcel = apiResult.data

                    SopoLog.d(
                        """
                        getRemoteParcel() success
                        parcel >>> $parcel
                    """.trimIndent()
                    )

                    if (parcel == null)
                    {
                        _result.postValue(TestResult.ErrorResult<String>(code = ResponseCode.SEARCH_PARCEL_NULL_ERROR, errorMsg = "택배 정보를 불러오는데 실패했습니다.", errorType = ResultTypeConst.ErrorType.ERROR_TYPE_DIALOG, data = null, e = null))
                        return@launch
                    }

                    _parcelEntity.postValue(ParcelMapper.parcelToParcelEntity(parcel))

                    withContext(Dispatchers.Default) {
                        updateIsBeUpdate(parcelId, StatusConst.DEACTIVATE)
                    }
                }
                is NetworkResult.Error ->
                {
//                    SopoLog.e("error >>> ${(result.exception as APIException)}", (result.exception as APIException).t)
                }
            }
        }
    }

    private suspend fun updateIsBeUpdate(parcelId:Int, status: Int?)
    {
        parcelManagementRepoImpl.updateUpdatableStatus(parcelId, status)
    }

    // TODO 널러블 확인
    private suspend fun isBeUpdateParcel(): LiveData<Int?>
    {
        return parcelRepository.isBeingUpdateParcel(
            parcelId = parcelId.value?:1000
        )
    }

    fun onBackClicked()
    {
        isBack.value = true
    }

    //
    fun updateIsUnidentifiedToZero(parcelId:Int)
    {
        CoroutineScope(Dispatchers.Default).launch {

            val value = parcelManagementRepoImpl.getUnidentifiedStatusByParcelId(parcelId = parcelId)

            SopoLog.d("update 정상적으로 값이 들어오는지 체크 $value")

            if (value == StatusConst.ACTIVATE)
            {
                parcelManagementRepoImpl.updateIsUnidentified(
                    parcelId = parcelId, value = StatusConst.DEACTIVATE
                )
            }
        }
    }

    fun onDownClicked(): View.OnClickListener
    {
        return View.OnClickListener() {
            isDown.value = true
        }
    }
}