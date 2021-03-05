package com.delivery.sopo.viewmodels.inquiry

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.consts.DeliveryStatusConst
import com.delivery.sopo.database.room.entity.ParcelEntity
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.consts.ResultTypeConst
import com.delivery.sopo.consts.UpdateConst
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.mapper.ParcelMapper
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.TestResult
import com.delivery.sopo.models.parcel.*
import com.delivery.sopo.networks.call.ParcelCall
import com.delivery.sopo.repository.impl.CourierRepoImpl
import com.delivery.sopo.repository.impl.ParcelManagementRepoImpl
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.livedates.SingleLiveEvent
import com.delivery.sopo.views.adapter.TimeLineRvAdapter
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ParcelDetailViewModel(
    private val userRepoImpl: UserRepoImpl,
    private val courierRepoImpl: CourierRepoImpl,
    private val parcelRepoImpl: ParcelRepoImpl,
    private val parcelManagementRepoImpl: ParcelManagementRepoImpl
) : ViewModel()
{
    // 택배 인포의 pk
    val parcelId = MutableLiveData<ParcelId>()

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
    var deliveryStatusMsg = MutableLiveData<String>()
    var deliveryStatusBg = MutableLiveData<Int>()

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
    val result : LiveData<TestResult>
    get() = _result

    init
    {
        deliveryStatusMsg.value = ""
        deliveryStatusBg.value = 0
        _isProgress.value = true
    }

    // Full Detail View의 리사이클러뷰 어댑터 세팅
    private fun setAdapter(list: List<Progress?>)
    {
        val timeLineRvAdapter = TimeLineRvAdapter()
        timeLineRvAdapter.setItemList(list as MutableList<Progress?>)
        adapter.postValue(timeLineRvAdapter)
    }

    // 택배 상세 UI 세팅
    fun updateParcelItem(parcelEntity: ParcelEntity)
    {
        SopoLog.d("updateParcelItem() call")
        var progressList: MutableList<Progress> = mutableListOf()
        var deliveryStatus = ""

        CoroutineScope(Dispatchers.Main).launch {

            withContext(Dispatchers.Default) {

                parcelItem = Gson().fromJson<ParcelItem>(parcelEntity.inquiryResult, ParcelItem::class.java)

                SopoLog.d( msg = "==>> ${parcelEntity.toString()}")
                SopoLog.d( msg = "==>> ${parcelItem.toString()}")
                //----------------------------------------------------------------------------------
            }

            withContext(Dispatchers.IO) {
                // Delivery Status
                deliveryStatus = when (parcelEntity.deliveryStatus)
                {
                    DeliveryStatusConst.NOT_REGISTER ->
                    {
                        deliveryStatusMsg.postValue("아직 배송상품 정보가 없습니다.")
                        deliveryStatusBg.postValue(R.drawable.ic_parcel_not_register)
                        "미등록"
                    }

                    DeliveryStatusConst.INFORMATION_RECEIVED ->
                    {
                        deliveryStatusMsg.postValue("아직 배송상품 정보가 없습니다.")
                        deliveryStatusBg.postValue(R.drawable.ic_parcel_not_register)
                        "배송정보 접수"
                    }
                    DeliveryStatusConst.AT_PICKUP ->
                    {
                        deliveryStatusMsg.postValue("상품이 집화처리 되었습니다.")
                        deliveryStatusBg.postValue(R.drawable.ic_parcel_at_pickup)
                        "상품픽업"
                    }
                    DeliveryStatusConst.IN_TRANSIT ->
                    {
                        deliveryStatusMsg.postValue("상품이 출발했습니다.")
                        deliveryStatusBg.postValue(R.drawable.ic_parcel_in_transit)
                        "배송중"
                    }
                    DeliveryStatusConst.OUT_FOR_DELIVERRY ->
                    {
                        deliveryStatusMsg.postValue("집배원이 배달을 시작했습니다.")
                        deliveryStatusBg.postValue(R.drawable.ic_parcel_out_for_delivery)
                        "동네도착"
                    }
                    DeliveryStatusConst.DELIVERED ->
                    {
                        deliveryStatusMsg.postValue("상품이 도착했습니다.")
                        deliveryStatusBg.postValue(R.drawable.ic_parcel_delivered)
                        "배송완료"
                    }
                    else ->
                    {
                        deliveryStatusMsg.postValue("상품을 조회할 수 없습니다.")
                        deliveryStatusBg.postValue(0)
                        SopoLog.d( msg = parcelEntity.deliveryStatus)
                        "에러상태"
                    }
                }

                // 배경 및 배송 상태 표시용
                statusList.postValue(getDeliveryStatusIndicator(deliveryStatus = parcelEntity.deliveryStatus))
                //--------------------------------------------------------------------------------------------
            }

            /*
            택배 정보의 별칭

            alias가 default가 아닐 때 해당 alias를 표기

            alias가 default일 때
                보낸 이(from)의 값이 존재한다면 그것으로 대체
                없다면 PlaceHolder("택배의 별칭을 등록해주세요.")로 설정
             */
            val alias =
                if (parcelEntity.parcelAlias != "default")
                {
                    parcelEntity.parcelAlias
                }
                else
                {
                    if (parcelItem != null)
                        "${parcelItem!!.from!!.name}이 보낸 택배"
                    else
                        "택배의 별칭을 등록해주세요."
                }
            //--------------------------------------------------------------------------------------

            withContext(Dispatchers.IO)
            {
                // ParcelEntity의 택배사 코드를 이용하여 택배사 정보를 로컬 DB에서 읽어온다.
                val courier = courierRepoImpl.getWithCode(parcelEntity.carrier)

                // 프로그레스(택배의 경로 내용이 있을 때 RecyclerView 없을 땐 텍스트로 없다고 표시)
                if (parcelItem != null)
                {
                    progressList = mutableListOf<Progress>()

                    for (item in parcelItem!!.progresses)
                    {
                        val date = DateUtil.changeDateFormat(item!!.time!!)
                        val spliteDate = date!!.split(" ")
                        val dateObj = Date(spliteDate[0], spliteDate[1])
                        val progress = Progress(
                            date = dateObj,
                            location = item.location!!.name,
                            description = item.description,
                            status = item.status
                        )

                        progressList.add(progress)
                    }
                }

                item.postValue(
                    ParcelDetailItem(
                        regDt = parcelEntity.regDt,
                        alias = alias,
                        courier = courier!!,
                        waybilNym = parcelEntity.trackNum,
                        deliverStatus = deliveryStatus,
                        progress = progressList
                    )
                )
            }

            SopoLog.d( msg = "Detail Item => ${item.value}")

            setAdapter(progressList)
        }
    }

    // 택배의 이동 상태(indicator)의 값을 리스트 형식으로 반환 / true => 현재 상태
    private fun getDeliveryStatusIndicator(deliveryStatus: String): MutableList<SelectItem<String>>
    {
        val statusList = mutableListOf<SelectItem<String>>(
            SelectItem("상품픽업", false),
            SelectItem("배송중", false),
            SelectItem("동네도착", false),
            SelectItem("배송완료", false)
        )

        when (deliveryStatus)
        {
            DeliveryStatusConst.INFORMATION_RECEIVED -> statusList[0].isSelect = true
            DeliveryStatusConst.AT_PICKUP -> statusList[0].isSelect = true
            DeliveryStatusConst.IN_TRANSIT -> statusList[1].isSelect = true
            DeliveryStatusConst.OUT_FOR_DELIVERRY -> statusList[2].isSelect = true
            DeliveryStatusConst.DELIVERED -> statusList[3].isSelect = true
        }

        return statusList
    }

    // 로컬에 저장된 택배 인포를 로드
    private fun requestLocalParcel(parcelId: ParcelId)
    {
        SopoLog.d("requestLocalParcel(${parcelId}) call")

        CoroutineScope(Dispatchers.Default).launch {

            // 로컬 데이터 옵저빙
            _parcelEntity.postValue(parcelRepoImpl.getLocalParcelById(parcelId = parcelId))
            _isProgress.postValue(false)
        }
    }

    // remote data를 요청과 동시에
    fun requestRemoteParcel(parcelId: ParcelId)
    {
        SopoLog.d("requestRemoteParcel(${parcelId}) call")

        requestRemoteParcel(parcelId)

        // remote data를 갱신하도록 서버에 요
        try{
            CoroutineScope(Dispatchers.IO).launch {
                when(val result = ParcelCall.requestParcelForRefresh(parcelId))
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
                        when(result.statusCode)
                        {
                            204 ->
                            {
                                // 업데이트 할게 없음
                                SopoLog.d("상세 내역 업데이트할 요소가 없습니다.")
                                _isUpdate.postValue(UpdateConst.FAILURE)
                            }
                            303 ->
                            {
                                SopoLog.d("상세 내역 업데이트할 요소가있습니다.")
                                _isUpdate.postValue(UpdateConst.SUCCESS)
                            }
                            else ->
                            {
                                SopoLog.e("${error.responseMessage}", error)
                            }
                        }
                    }
                }
            }
        } finally
        {

        }

    }

    fun getRemoteParcel(parcelId: ParcelId)
    {
        SopoLog.d("getRemoteParcel() call >>> ${parcelId.toString()}")

        CoroutineScope(Dispatchers.IO).launch {
            when(val result = ParcelCall.getSingleParcel(parcelId = parcelId))
            {
                is NetworkResult.Success ->
                {
                    val apiResult = result.data
                    val parcel = apiResult.data

                    SopoLog.d("""
                        getRemoteParcel() success
                        parcel >>> $parcel
                    """.trimIndent())

                    if(parcel == null)
                    {
                        _result.postValue(TestResult.ErrorResult<String>(code = ResponseCode.SEARCH_PARCEL_NULL_ERROR, errorMsg = "택배 정보를 불러오는데 실패했습니다.", errorType = ResultTypeConst.ErrorType.ERROR_TYPE_DIALOG, data = null, e = null))
                        return@launch
                    }

                    _parcelEntity.postValue(ParcelMapper.parcelToParcelEntity(parcel))

                    withContext(Dispatchers.Default)
                    {
                        updateIsBeUpdate(parcelId, UpdateConst.DEACTIVATE)
                    }
                }
                is NetworkResult.Error ->
                {
                    SopoLog.e("error >>> ${(result.exception as APIException)}", (result.exception as APIException).t)
                }
            }
        }
    }

    private suspend fun updateIsBeUpdate(parcelId: ParcelId, status : Int?)
    {
        parcelManagementRepoImpl.updateIsBeUpdate(parcelId.regDt, parcelId.parcelUid, status)
    }

    private suspend fun isBeUpdateParcel(): LiveData<Int?>
    {
        return parcelRepoImpl.isBeingUpdateParcel(
            parcelUid = parcelId.value?.parcelUid ?: "",
            regDt = parcelId.value?.regDt ?: ""
        )
    }

    fun onBackClicked()
    {
        isBack.value = true
    }

    //
    fun updateIsUnidentifiedToZero(parcelId: ParcelId)
    {
        CoroutineScope(Dispatchers.Default).launch {

            val value = parcelManagementRepoImpl.getIsUnidentifiedByParcelId(parcelId = parcelId)

            SopoLog.d("update 정상적으로 값이 들어오는지 체크 $value")

            if (value == UpdateConst.ACTIVATE)
            {
                parcelManagementRepoImpl.updateIsUnidentified(
                    parcelId = parcelId,
                    value = UpdateConst.DEACTIVATE
                )
            }
        }
    }

    fun onDownClicked(): View.OnClickListener
    {
        return View.OnClickListener()
        {
            isDown.value = true
        }
    }
}