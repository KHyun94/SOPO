package com.delivery.sopo.viewmodels.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.delivery.sopo.consts.StatusConst
import com.delivery.sopo.data.repository.database.room.entity.AppPasswordEntity
import com.delivery.sopo.data.repository.database.room.entity.ParcelEntity
import com.delivery.sopo.data.repository.local.app_password.AppPasswordRepository
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.firebase.FirebaseNetwork
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*

class MainViewModel(private val userRepo: UserLocalRepository, private val parcelRepo: ParcelRepository, private val parcelManagementRepoImpl: ParcelManagementRepoImpl, private val appPasswordRepo: AppPasswordRepository):
        ViewModel()
{
    val mainTabVisibility = MutableLiveData<Int>()

    // 앱 패스워드 등록 여부
    /*val isSetAppPassword: LiveData<AppPasswordEntity?>
        get() = appPasswordRepo.getByLiveData()
*/
    // 유효성 및 통신 등의 결과 객체
    private var _result = MutableLiveData<ResponseResult<*>>()
    val result: LiveData<ResponseResult<*>>
        get() = _result

    init
    {
        CoroutineScope(Dispatchers.Main).launch {

            try
            {
                // 1. 업데이트 가능한 택배가 있는지 확인
                val isUpdatableParcelStatus = isUpdatableParcelStatus()

                // 2. 업데이트할 택배가 없다면 서버를 통해 갱신 처리를 하지 않는다.
                if(!isUpdatableParcelStatus)
                {
                    SopoLog.d("업데이트 가능 택배가 존재하지 않음")
                    return@launch
                }

                requestOngoingParcels()
            }
            finally
            {
                checkSubscribedTime()
            }

        }
        updateFCMToken()
    }

    fun setMainTabVisibility(visibility: Int)
    {
        mainTabVisibility.postValue(visibility)
    }

    // 업데이트 가능한 택배가 있는지 체크 [ParcelStatusEntity - updatableStatus
    private suspend fun isUpdatableParcelStatus(): Boolean = withContext(Dispatchers.Default) {
        SopoLog.d("isUpdatableParcelStatus() call")
        parcelManagementRepoImpl.getCountForUpdatableParcel() > 0
    }

    private suspend fun insertRemoteParcel(entity: ParcelEntity)
    {
        withContext(Dispatchers.Default) {
            SopoLog.i("insertRemoteParcel(...) 호출")

            parcelRepo.insetEntity(entity)
            val parcelStatusEntity =
                ParcelMapper.parcelEntityToParcelManagementEntity(entity).apply {
                    unidentifiedStatus = 1
                }
            parcelManagementRepoImpl.insertEntity(parcelStatusEntity = parcelStatusEntity)
        }
    }

    /**
     * DB 서버 내에 저장된 진행 중인 택배 데이터를 기반으로
     * 로컬 DB에 저장된 택배 데이터를 갱신
     */
    suspend fun requestOngoingParcels()
    {
        SopoLog.i("refreshOngoingParcel(...) 호출")

        // 1. 서버로부터 택배 데이터를 호출
        val remoteParcels =
            parcelRepo.getRemoteOngoingParcels() ?: return SopoLog.d("업데이트할 택배 데이터가 없습니다.")

        for(remoteParcel in remoteParcels)
        {
            SopoLog.d("업데이트 예정 Parcel[remote:   ${remoteParcel.toString()}]")

            //2. 서버에서 불러온 택배 데이터 기준으로 로컬 내 저장된 택배 데이터를 호출
            val localParcel = parcelRepo.getLocalParcelById(remoteParcel.parcelId).let { entity ->
                if(entity == null)
                {
                    withContext(Dispatchers.Default) {
                        val remoteParcelEntity = ParcelMapper.parcelToParcelEntity(remoteParcel)
                        insertRemoteParcel(remoteParcelEntity)
                    }

                    return@let remoteParcel
                }

                ParcelMapper.parcelEntityToParcel(entity)
            }

            SopoLog.d("업데이트 예정 Parcel[local:    ${localParcel.toString()}]")

            // 3. Status가 1이 아닌 택배들은 업데이트 제외
            if(localParcel.status != StatusConst.ACTIVATE)
            {
                SopoLog.d("해당 택배는 Status가 [Status:${localParcel.status}]임으로 업데이트 제외")
                continue
            }

            // 4. inquiryHashing이 같은 것, 즉 이전 내용과 다름 없을 땐 update 하지 않는다.
            if((localParcel.inquiryHash == remoteParcel.inquiryHash))
            {
                SopoLog.d("해당 택배는 inquiryHashing이 같은 것, 즉 이전 내용과 다름 없기 때문에 업데이트 제외")
                continue
            }

            val remoteParcelEntity = ParcelMapper.parcelToParcelEntity(remoteParcel)

            val remoteParcelStatusEntity =
                ParcelMapper.parcelEntityToParcelManagementEntity(remoteParcelEntity).apply {

                    if(localParcel.deliveryStatus != remoteParcel.deliveryStatus)
                    {
                        SopoLog.d(
                            "해당 택배는 상태가 [${localParcel.deliveryStatus} -> ${remoteParcel.deliveryStatus}]로 변경")
                        unidentifiedStatus = 1
                    }

                    // unidentifiedStatus가 이미 활성화 상태이면 비활성화 조치
                    val isUnidentifiedStatus = withContext(Dispatchers.Default) {
                        parcelManagementRepoImpl.getUnidentifiedStatusByParcelId(
                            localParcel.parcelId) == 1
                    }

                    if(isUnidentifiedStatus)
                    {
                        unidentifiedStatus = 0
                    }

                    updatableStatus = 0
                }

            withContext(Dispatchers.Default) {
                parcelRepo.updateEntity(remoteParcelEntity)
                parcelManagementRepoImpl.updateEntity(remoteParcelStatusEntity)
            }

            SopoLog.d("업데이트 완료 [parcel id:${remoteParcel.parcelId}]")
        }
    }

    /** Update FCM Token  **/
    private fun updateFCMToken()
    {
        SopoLog.d(msg = "updateFCMToken 호출 ")

        FirebaseNetwork.updateFCMToken()
    }

    suspend fun checkSubscribedTime() = withContext(Dispatchers.Default) {
        SopoLog.i("checkSubscribedTime(...) 호출")

        val ongoingParcelCnt = parcelRepo.getOnGoingDataCnt()

        SopoLog.d("현재 진행 중인 택배 갯수 [data:$ongoingParcelCnt(개)]")

        val subscribedTopic = userRepo.getTopic()

        SopoLog.d("구독 중인 시간 [data:$subscribedTopic]")

        if(subscribedTopic == "" && ongoingParcelCnt > 0)
        {
            SopoLog.d("구독 중인 시간이 없고, 현재 진행 중인 택배가 있는 상태")
            FirebaseNetwork.subscribedToTopicInFCM()
        }
    }
}