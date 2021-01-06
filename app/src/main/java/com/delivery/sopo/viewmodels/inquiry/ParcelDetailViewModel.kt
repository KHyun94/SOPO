package com.delivery.sopo.viewmodels.inquiry

import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.consts.DeliveryStatusConst
import com.delivery.sopo.database.room.entity.ParcelEntity
import com.delivery.sopo.mapper.ParcelMapper
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.parcel.*
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI
import com.delivery.sopo.repository.impl.CourierRepolmpl
import com.delivery.sopo.repository.impl.ParcelManagementRepoImpl
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.livedates.SingleLiveEvent
import com.delivery.sopo.views.adapter.TimeLineRvAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ParcelDetailViewModel(
    private val userRepoImpl: UserRepoImpl,
    private val courierRepolmpl: CourierRepolmpl,
    private val parcelRepoImpl: ParcelRepoImpl,
    private val parcelManagementRepoImpl: ParcelManagementRepoImpl
) : ViewModel()
{
    val TAG = "LOG.SOPO"

    // 택배 인포의 pk
    val parcelId = MutableLiveData<ParcelId?>()

    // delivery status 리스트
    val statusList = MutableLiveData<MutableList<SelectItem<String>>?>()
    var adapter = MutableLiveData<TimeLineRvAdapter?>()

    private val _parcelEntity = MutableLiveData<ParcelEntity?>()
    val parcelEntity: LiveData<ParcelEntity?>
        get() = _parcelEntity

    // parcelEntity 중 inqueryResult를 객체화시키는 용도
    var parcelItem: ParcelItem? = null

    // 상세 화면에서 사용할 데이터 객체
    var item = MutableLiveData<ParcelDetailItem?>()

    var subTitle = MutableLiveData<String>()
    var statusBg = MutableLiveData<Int?>()

    // 상세 화면 종료
    var isBack = SingleLiveEvent<Boolean>()

    // 상세 화면 Full Down
    var isDown = SingleLiveEvent<Boolean>()

    var isUpdate = MutableLiveData<Boolean?>()

    // todo 상세 조회단 임시 업데이트 객체
    var tmpParcel: ParcelEntity? = null

    init
    {
        subTitle.value = ""
    }

    // todo java.lang.ClassCastException: com.google.gson.internal.LinkedTreeMap cannot be cast to 해당 에러 발생해서 사용 불가
    fun <T> changeJsonToObject(json: String): T
    {
        val gson = Gson()

        val type = object : TypeToken<T>()
        {}.type

        val reader = gson.toJson(json)
        val replaceStr = reader.replace("\\", "")
        val subStr = replaceStr.substring(1, replaceStr.length - 1)

        return gson.fromJson<T>(subStr, type)
    }

    // 택배 상세 UI 세팅
    fun updateParcelItem(parcelEntity: ParcelEntity)
    {
        var progressList: MutableList<Progress?> = mutableListOf()
        var deliveryStatus = ""

        CoroutineScope(Dispatchers.Main).launch {

            withContext(Dispatchers.Default) {

                // ParcelEntity 중 inqueryResult(json의 String화)를 ParcelItem으로 객체화
                val gson = Gson()

                val type = object : TypeToken<ParcelItem?>()
                {}.type

                val reader = gson.toJson(parcelEntity.inqueryResult)
                val replaceStr = reader.replace("\\", "")
                val subStr = replaceStr.substring(1, replaceStr.length - 1)

                parcelItem = gson.fromJson<ParcelItem?>(subStr, type)

                SopoLog.d(tag = TAG, msg = "==>> ${parcelEntity.toString()}")
                SopoLog.d(tag = TAG, msg = "==>> ${parcelItem.toString()}")
                //----------------------------------------------------------------------------------
            }

            withContext(Dispatchers.IO) {
                // Delivery Status
                deliveryStatus = when (parcelEntity.deliveryStatus)
                {
                    DeliveryStatusConst.NOT_REGISTER ->
                    {
                        subTitle.postValue("아직 배송상품 정보가 없습니다.")
                        statusBg.postValue(R.drawable.ic_parcel_not_register)
                        "미등록"
                    }

                    DeliveryStatusConst.INFORMATION_RECEIVED ->
                    {
                        subTitle.postValue("아직 배송상품 정보가 없습니다.")
                        statusBg.postValue(R.drawable.ic_parcel_not_register)
                        "배송정보 접수"
                    }
                    DeliveryStatusConst.AT_PICKUP ->
                    {
                        subTitle.postValue("상품이 집화처리 되었습니다.")
                        statusBg.postValue(R.drawable.ic_parcel_at_pickup)
                        "상품픽업"
                    }
                    DeliveryStatusConst.IN_TRANSIT ->
                    {
                        subTitle.postValue("상품이 출발했습니다.")
                        statusBg.postValue(R.drawable.ic_parcel_in_transit)
                        "배송중"
                    }
                    DeliveryStatusConst.OUT_FOR_DELIVERRY ->
                    {
                        subTitle.postValue("집배원이 배달을 시작했습니다.")
                        statusBg.postValue(R.drawable.ic_parcel_out_for_delivery)
                        "동네도착"
                    }
                    DeliveryStatusConst.DELIVERED ->
                    {
                        subTitle.postValue("상품이 도착했습니다.")
                        statusBg.postValue(R.drawable.ic_parcel_delivered)
                        "배송완료"
                    }
                    else ->
                    {
                        subTitle.postValue("상품을 조회할 수 없습니다.")
                        statusBg.postValue(0)
                        SopoLog.d(tag = TAG, msg = parcelEntity.deliveryStatus)
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
                val courier = courierRepolmpl.getWithCode(parcelEntity.carrier)

                // 프로그레스(택배의 경로 내용이 있을 때 RecyclerView 없을 땐 텍스트로 없다고 표시)
                if (parcelItem != null)
                {
                    progressList = mutableListOf<Progress?>()

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

            SopoLog.d(tag = TAG, msg = "Detail Item => ${item.value}")

            setAdapter(progressList)
        }
    }

    // 택배의 이동 상태(indicator)의 값을 리스트 형식으로 반환 / true => 현재 상태
    private fun getDeliveryStatusIndicator(deliveryStatus: String): MutableList<SelectItem<String>>
    {
        val _statusList = mutableListOf<SelectItem<String>>(
            SelectItem("상품픽업", false),
            SelectItem("배송중", false),
            SelectItem("동네도착", false),
            SelectItem("배송완료", false)
        )

        when (deliveryStatus)
        {
            DeliveryStatusConst.INFORMATION_RECEIVED ->
            {
                _statusList[0].isSelect = true
            }
            DeliveryStatusConst.AT_PICKUP ->
            {
                _statusList[0].isSelect = true
            }
            DeliveryStatusConst.IN_TRANSIT ->
            {
                _statusList[1].isSelect = true
            }
            DeliveryStatusConst.OUT_FOR_DELIVERRY ->
            {
                _statusList[2].isSelect = true
            }
            DeliveryStatusConst.DELIVERED ->
            {
                _statusList[3].isSelect = true
            }
            else ->
            {
                // 못가져오거나 미등록 상태
            }
        }

        return _statusList
    }


    fun requestParcelDetailData(parcelId: ParcelId)
    {
        // 서버로 해당 택배 상태 업데이트 요청 -> 서비스로 조회
        parcelId.let {
            requestRemoteParcel(parcelId = it)
            requestLocalParcel(parcelId = it)
        }
    }

    // 로컬에 저장된 택배 인포를 로드
    private fun requestLocalParcel(parcelId: ParcelId)
    {
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
            }
        }
    }

    private fun requestRemoteParcel(parcelId: ParcelId)
    {
        NetworkManager.privateRetro.create(ParcelAPI::class.java)
            .parcelRefreshing(
                email = userRepoImpl.getEmail(),
                parcelUid = parcelId.parcelUid,
                regDt = parcelId.regDt
            ).enqueue(object : Callback<APIResult<Parcel?>>
            {
                override fun onFailure(call: Call<APIResult<Parcel?>>, t: Throwable)
                {
                    SopoLog.e(msg = "택배 상세 내역 에러 => ${t.message}", e = t)
                    isUpdate.postValue(false)
                }

                override fun onResponse(
                    call: Call<APIResult<Parcel?>>,
                    response: Response<APIResult<Parcel?>>
                )
                {
                    when (response.code())
                    {
                        200 ->
                        {
                            /*
                                todo 현재 성공 시 업데이트 여부 스낵바 호출

                                todo 업데이트 시
                                  isBeUpdate, isUnidentified는 업데이트 필요 x
                                  UI 업데이트 및 ROOM에 저장

                                 todo 업데이트 X
                                  parcel_management - isBeupdate -> 1로 업데이트
                             */
                            val res = response.body()
                            val parcel = res!!.data ?: return

                            SopoLog.d(msg = "택배 상세 내역 결과 값 => $parcel")

                            if (res.message == "CHANGED")
                            {
                                // 업데이트 변경 가능 true 일 때 스낵바를 호출
                                isUpdate.postValue(true)
                                updateIsBeUpdate(parcelId.regDt, parcelId.parcelUid, 1)
                                tmpParcel = ParcelMapper.parcelToParcelEntity(parcel)
                            }
                            else
                            {
                                // 업데이트 할게 없음
                                isUpdate.postValue(null)
                            }
                        }
                        204 ->
                        {
                            // 업데이트 할게 없음
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

                            isUpdate.postValue(true)
                        }

                        400 ->
                        {
                            SopoLog.d(tag = TAG, msg = "택배 상세 내역 에러 => ${response.errorBody()}")
                            isUpdate.postValue(false)
                        }
                        else ->
                        {
                            SopoLog.d(tag = TAG, msg = "택배 상세 내역 에러 => ${response.errorBody()}")
                            isUpdate.postValue(false)
                        }
                    }
                }
            })
    }

    fun getRemoteParcel()
    {
        NetworkManager.privateRetro.create(ParcelAPI::class.java)
            .getOneParcel(userRepoImpl.getEmail(), parcelId.value!!.regDt, parcelId.value!!.parcelUid)
            .enqueue(object : Callback<APIResult<Parcel?>>{
                override fun onFailure(call: Call<APIResult<Parcel?>>, t: Throwable)
                {
                    SopoLog.e(msg = "GET 단일 택배 실패", e = t)
                }

                override fun onResponse(
                    call: Call<APIResult<Parcel?>>,
                    response: Response<APIResult<Parcel?>>
                )
                {
                    // ROOM에 업데이트
                    //
                    updateParcelItem(tmpParcel!!)

                    val httpStatusCode = response.code()

                    Log.d("LOG.SOPO", "뭐지? $httpStatusCode")

                    // http status code 200
                    when (httpStatusCode)
                    {
                        200 ->
                        {
                            _parcelEntity.postValue(tmpParcel)
                            updateIsBeUpdate(parcelId.value!!.regDt, parcelId.value!!.parcelUid, 0)
                        }
                        400 ->
                        {
                        }
                        else ->
                        {
                            Log.d("LOG.SOPO", "알 수 없는 에러!!!!")
                        }
                    }
                }

            })

    }


    // Full Detail View의 리사이클러뷰 어댑터 세팅
    private fun setAdapter(list: List<Progress?>)
    {
        val timeLineRvAdapter = TimeLineRvAdapter()
        timeLineRvAdapter.setItemList(list as MutableList<Progress?>)
        adapter.postValue(timeLineRvAdapter)
    }

    fun updateIsBeUpdate(regDt: String, parcelUid: String, status : Int?)
    {
        CoroutineScope(Dispatchers.Default).launch {
            parcelManagementRepoImpl.updateIsBeUpdate(regDt, parcelUid, status)
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