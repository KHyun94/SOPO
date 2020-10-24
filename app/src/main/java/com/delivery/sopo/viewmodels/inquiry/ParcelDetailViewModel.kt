package com.delivery.sopo.viewmodels.inquiry

import android.util.Log
import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.R
import com.delivery.sopo.consts.DeliveryStatusConst
import com.delivery.sopo.database.room.entity.ParcelEntity
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.parcel.*
import com.delivery.sopo.models.parcel.Date
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI
import com.delivery.sopo.repository.impl.CourierRepolmpl
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
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
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ParcelDetailViewModel(
    private val userRepoImpl: UserRepoImpl,
    private val courierRepolmpl: CourierRepolmpl,
    private val parcelRepoImpl: ParcelRepoImpl
) : ViewModel()
{
    val TAG = "LOG.SOPO"

    // 택배 인포의 pk
    val parcelId = MutableLiveData<ParcelId?>()

    // delivery status 리스트
    val statusList = MutableLiveData<MutableList<SelectItem<String>>?>()
    var adapter = MutableLiveData<TimeLineRvAdapter?>()

    // Local or Remote Parcel Data를 저장하는 객체
//    var parcelEntity: ParcelEntity? = null
    var parcelEntity: ParcelEntity? = null

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

    fun updateParcelItem(parcelEntity: ParcelEntity)
    {
        var progressList: MutableList<Progress?> = mutableListOf()
        var deliveryStatus = ""

        CoroutineScope(Dispatchers.Main).launch {

            withContext(Dispatchers.Default) {
                // 로컬에 등록된 택배 정보를 불러온다.

                // ParcelEntity 중 inqueryResult(json의 String화)를 ParcelItem으로 객체화
                val gson = Gson()

                val type = object : TypeToken<ParcelItem?>()
                {}.type

                val reader = gson.toJson(parcelEntity!!.inqueryResult)
                val replaceStr = reader.replace("\\", "")
                val subStr = replaceStr.substring(1, replaceStr.length - 1)

                parcelItem = gson.fromJson<ParcelItem?>(subStr, type)

                Log.d(TAG, "==>> ${parcelEntity!!.toString()}")
                Log.d(TAG, "==>> ${parcelItem.toString()}")
                //----------------------------------------------------------------------------------
            }

            withContext(Dispatchers.IO) {
                // Delivery Status
                deliveryStatus = when (parcelEntity!!.deliveryStatus)
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
                        statusBg.postValue(R.drawable.ic_parcel_out_of_delivery_dismiss)
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
                        Log.d(TAG, parcelEntity!!.deliveryStatus)
                        "에러상태"
                    }
                }


                // 배경 및 배송 상태 표시용
                statusList.postValue(getDeliveryStatusIndicator(deliveryStatus = parcelEntity!!.deliveryStatus))
                //--------------------------------------------------------------------------------------------
            }

            // 택배 정보의 별칭
            val alias =
                if (parcelEntity!!.parcelAlias != "default")
                {
                    parcelEntity!!.parcelAlias
                }
                else
                {
                    if (parcelItem != null)
                        "${parcelItem!!.from!!.name}이 보낸 택배"
                    else
                        "택배의 별칭을 등록해주세요."
                }

            withContext(Dispatchers.IO)
            {
                // 택배사 코드를 ROOM에서 택배명으로 검색
                val courier = courierRepolmpl.getWithCode(parcelEntity!!.carrier)



                // 프로그레스(택배의 경로 내용이 있을 때
                if (parcelItem != null)
                {
                    progressList = mutableListOf<Progress?>()

                    for (item in parcelItem!!.progresses)
                    {
                        val date = changeDateFormat(item!!.time!!)
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
                        regDt = parcelEntity!!.regDt,
                        alias = alias,
                        courier = courier!!,
                        waybilNym = parcelEntity!!.trackNum,
                        deliverStatus = deliveryStatus,
                        progress = progressList
                    )
                )
            }

            Log.d(TAG, "Detail Item => ${item.value}")

            setAdapter(progressList)
        }
    }

    // 로컬에 저장된 택배 인포를 로드
    fun requestLocalParcel(parcelId: ParcelId)
    {
        // 로컬 호출 동시에 서버에 택배 상태 업데이트 상태 체크
        requestRemoteParcel(parcelId = parcelId)

        CoroutineScope(Dispatchers.Main).launch{
            withContext(Dispatchers.Default){
                parcelEntity = parcelRepoImpl.getLocalParcelById(
                    parcelUid = parcelId.parcelUid,
                    regDt = parcelId.regDt
                )
            }

            updateParcelItem(parcelEntity!!)
        }
    }

    // 택배의 이동 상태(indicator)의 값을 리스트 형식으로 반환
    fun getDeliveryStatusIndicator(deliveryStatus: String): MutableList<SelectItem<String>>
    {
        val _statusList = mutableListOf<SelectItem<String>>(
            SelectItem("상품픽업", false),
            SelectItem("배송중", false),
            SelectItem("동네도착", false),
            SelectItem("배송완료", false)
        )

        when (deliveryStatus)
        {
            DeliveryStatusConst.NOT_REGISTER ->
            {

            }
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

    // dateTime => yyyy-MM-dd'T'HH"mm:ss.SSS'Z -> yyyy-MM-dd HHmm
    fun changeDateFormat(dateTime: String): String?
    {
        val oldFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssX")
        oldFormat.timeZone = TimeZone.getTimeZone("KST")
        val newFormat = SimpleDateFormat("yy/MM/dd HH:mm:ss")

        return try
        {
            val oldDate = oldFormat.parse(dateTime)
            newFormat.format(oldDate)
        }
        catch (e: ParseException)
        {
            Log.e(TAG, e.toString())
            null
        }
    }


    private fun requestRemoteParcel(parcelId: ParcelId)
    {
        NetworkManager.privateRetro.create(ParcelAPI::class.java)
            .requestRenewalOneParcel(
                email = userRepoImpl.getEmail(),
                parcelUid = parcelId.parcelUid,
                regDt = parcelId.regDt
            ).enqueue(object : Callback<APIResult<Parcel?>>
            {
                override fun onFailure(call: Call<APIResult<Parcel?>>, t: Throwable)
                {
                    Log.d(TAG, "에러 ${t.message}")
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
                            val parcel = response.body()!!.data ?: return
                            Log.d(TAG, "정상 $parcel")
                            parcelEntity!!.update(parcel = parcel)
                            isUpdate.postValue(true)
                        }
                        400 -> Log.d(TAG, "정상 ${response.errorBody()}")
                        else -> Log.d(TAG, "에러러러")
                    }
                }

            })

    }

    private fun setAdapter(list: List<Progress?>)
    {
        val timeLineRvAdapter = TimeLineRvAdapter()
        timeLineRvAdapter.setItemList(list as MutableList<Progress?>)
        adapter.postValue(timeLineRvAdapter)
    }

    fun onBackClicked()
    {
        isBack.value = true
    }

    fun onDownClicked(): View.OnClickListener
    {
        return View.OnClickListener()
        {
            isDown.value = true
        }
    }
}