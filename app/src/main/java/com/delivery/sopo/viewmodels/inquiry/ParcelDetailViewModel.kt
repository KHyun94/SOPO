package com.delivery.sopo.viewmodels.inquiry

import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.consts.DeliveryStatusConst
import com.delivery.sopo.database.room.entity.ParcelEntity
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.SelectItem
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

    private val _parcelEntity = MutableLiveData<ParcelEntity?>()
    val parcelEntity: LiveData<ParcelEntity?>
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
    var isUpdate = MutableLiveData<Boolean?>()

    // todo 상세 조회단 임시 업데이트 객체
    var tmpParcel: ParcelEntity? = null

    private val _isProgress = MutableLiveData<Boolean>()
    val isProgress: LiveData<Boolean>
        get() = _isProgress

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

        CoroutineScope(Dispatchers.Main).launch {

            // 로컬 호출 동시에 서버에 택배 상태 업데이트 상태 체크
            withContext(Dispatchers.Default) {

                // 해당 ParcelEntity 라이브데이터를 옵저빙해서 UI 변환
                _parcelEntity.postValue(
                    // Parcel ID로 로컬 DB에 저장되어있는 Parcel 데이터를 호출
                    parcelRepoImpl.getLocalParcelById(
                        parcelUid = parcelId.parcelUid,
                        regDt = parcelId.regDt
                    )
                )

                _isProgress.postValue(false)
            }
        }
    }

    fun requestRemoteParcel(parcelId: ParcelId)
    {
        CoroutineScope(Dispatchers.IO).launch {
            when(val result = ParcelCall.requestRefreshParcel(parcelId))
            {
                is NetworkResult.Success ->
                {
                   SopoLog.d("success to requestRemoteParcel()")
                    requestLocalParcel(parcelId = parcelId)
                }
                is NetworkResult.Error ->
                {
                    val error = result.exception as APIException
                    SopoLog.e(msg = "(${result.statusCode})택배 상세 내역 에러 => (${error.responseCode}) ${error.responseCode}", e = error)
                    requestLocalParcel(parcelId = parcelId)

                    when(result.statusCode)
                    {
                        204 ->
                        {
                            // 업데이트 할게 없음
                            SopoLog.d("상세 내역 업데이트할 요소가 없습니다.")
                            isUpdate.postValue(null)
                        }
                        303 ->
                        {
                            /*
                           todo 업데이트 여부 스낵바 호출

                           todo 업데이트 시
                            Get parcel로 해당 택배 업데이트 및 ROOM에 저장
                            이 때 isUnidentified는 1로 업데이트 해 줄 필요가 없습니다.
                            UI 업데이트 및 ROOM에 저장

                           todo 업데이트 X
                            parcel_management - isBeupdate -> 1로 업데이트
                            */
                            SopoLog.d("상세 내역 업데이트할 요소가있습니다.")
                            isUpdate.postValue(true)
                        }
                        else ->
                        {
                            isUpdate.postValue(false)
                        }
                    }
                }
            }
        }
    }

    fun getRemoteParcel(parcelId: ParcelId)
    {
        CoroutineScope(Dispatchers.IO).launch {
            when(val result = ParcelCall.getSingleParcel(parcelId = parcelId))
            {
                is NetworkResult.Success ->
                {
                    _parcelEntity.postValue(tmpParcel)
                    updateIsBeUpdate(parcelId, 0)
                }
                is NetworkResult.Error ->
                {}
            }
        }
    }

    fun updateIsBeUpdate(parcelId: ParcelId, status : Int?)
    {
        CoroutineScope(Dispatchers.Default).launch {
            parcelManagementRepoImpl.updateIsBeUpdate(parcelId.regDt, parcelId.parcelUid, status)
        }
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

    fun updateIsUnidentifiedToZero(parcelId: ParcelId)
    {
        CoroutineScope(Dispatchers.Default).launch {

            val value = parcelManagementRepoImpl.getIsUnidentifiedByParcelId(
                regDt = parcelId.regDt,
                parcelUid = parcelId.parcelUid
            )

            if (value == 1)
            {
                val a = parcelManagementRepoImpl.updateIsUnidentified(
                    regDt = parcelId.regDt,
                    parcelUid = parcelId.parcelUid,
                    value = 0
                )
                SopoLog.d(msg = "update=>${a}")
            }
            else
            {
                val a = parcelManagementRepoImpl.updateIsUnidentified(
                    regDt = parcelId.regDt,
                    parcelUid = parcelId.parcelUid,
                    value = 0
                )
                SopoLog.d(msg = "Test update=>${a}")
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