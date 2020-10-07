package com.delivery.sopo.viewmodels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.consts.DeliveryStatusConst
import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.StatusItem
import com.delivery.sopo.models.entity.ParcelEntity
import com.delivery.sopo.models.parcel.*
import com.delivery.sopo.models.parcel.Date
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.ParcelAPI
import com.delivery.sopo.repository.CourierRepolmpl
import com.delivery.sopo.repository.ParcelRepoImpl
import com.delivery.sopo.repository.shared.UserRepo
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
    private val userRepo: UserRepo,
    private val courierRepolmpl: CourierRepolmpl,
    private val parcelRepoImpl: ParcelRepoImpl
) : ViewModel()
{
    val TAG = "LOG.SOPO"

    // 택배 인포의 pk
    val parcelId = MutableLiveData<ParcelId?>()

    // delivery status 리스트
    val statusList = MutableLiveData<MutableList<StatusItem>?>()
    var parcelEntity: ParcelEntity? = null

    var adapter = MutableLiveData<TimeLineRvAdapter?>()

    // parcelEntity 중 inqueryResult를 객체화시키는 용도
    var parcelItem: ParcelItem? = null

    var item = MutableLiveData<ParcelDetailItem?>()

    // 로컬에 저장된 택배 인포를 로드
    fun requestLocalParcel(parcelId: ParcelId)
    {
        // 로컬 호출 동시에 서버에 택배 상태 업데이트 상태 체크
        requestRemoteParcel(parcelId = parcelId)

        val _statusList = mutableListOf<StatusItem>(
            StatusItem("상품픽업", false),
            StatusItem("배송중", false),
            StatusItem("동네도착", false),
            StatusItem("배송완료", false)
        )

        var deliveryStatus = ""

        CoroutineScope(Dispatchers.Default).launch {
            withContext(Dispatchers.IO) {

                parcelEntity = parcelRepoImpl.getLocalParcelById(
                    parcelUid = parcelId.parcelUid,
                    regDt = parcelId.regDt
                )

                val gson = Gson()

                val type = object : TypeToken<ParcelItem?>()
                {}.type

                val reader = gson.toJson(parcelEntity!!.inqueryResult)
                val replaceStr = reader.replace("\\", "")
                val subStr = replaceStr.substring(1, replaceStr.length - 1)

                parcelItem = gson.fromJson<ParcelItem?>(subStr, type)

                Log.d(TAG, "==>> ${parcelEntity!!.toString()}")
                Log.d(TAG, "==>> ${parcelItem.toString()}")


                Log.d(TAG, "Origin Time ${parcelItem!!.from!!.time!!}")
                Log.d(TAG, "Revise Time ${changeDateFormat(parcelItem!!.from!!.time!!)}")


                // delivery status view value setting
                when (parcelEntity!!.deliveryStatus)
                {
                    DeliveryStatusConst.INFORMATION_RECEIVED ->
                    {
                        _statusList[0].isCurrent = true
                        deliveryStatus = _statusList[0].name
                    }
                    DeliveryStatusConst.AT_PICKUP ->
                    {
                        _statusList[0].isCurrent = true
                        deliveryStatus = _statusList[0].name
                    }
                    DeliveryStatusConst.IN_TRANSIT ->
                    {
                        _statusList[1].isCurrent = true
                        deliveryStatus = _statusList[1].name
                    }
                    DeliveryStatusConst.OUT_FOR_DELIVERRY ->
                    {
                        _statusList[2].isCurrent = true
                        deliveryStatus = _statusList[2].name
                    }
                    DeliveryStatusConst.DELIVERED ->
                    {
                        _statusList[3].isCurrent = true
                        deliveryStatus = _statusList[3].name
                    }
                    else ->
                    {
                        // 못가져오거나 미등록 상태
                    }
                }

                statusList.postValue(_statusList)

                val alias =
                    if (parcelEntity!!.parcelAlias == "default") "${parcelItem!!.from!!.name}이 보낸 택배" else parcelEntity!!.parcelAlias
                val courier = courierRepolmpl.getWithCode(parcelEntity!!.carrier)
                val progressList = mutableListOf<Progress?>()

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

                setAdapter(progressList)
            }
        }

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
                email = "gnltlgnlrl94@naver.com",
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
                        200 -> Log.d(TAG, "정상 ${response.body()}")
                        400 -> Log.d(TAG, "정상 ${response.errorBody()}")
                        else -> Log.d(TAG, "에러러러")
                    }
                }

            })

//            .requestRenewalOneParcel(email = userRepo.getEmail(), parcelUid = parcelUId, regDt = regDt)
    }

    private fun setAdapter(list: List<Progress?>)
    {
        val timeLineRvAdapter = TimeLineRvAdapter()
        timeLineRvAdapter.setItemList(list as MutableList<Progress?>)
        adapter.postValue(timeLineRvAdapter)
    }
}