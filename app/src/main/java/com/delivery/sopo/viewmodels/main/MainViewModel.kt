package com.delivery.sopo.viewmodels.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.data.repository.database.room.entity.AppPasswordEntity
import com.delivery.sopo.firebase.FirebaseNetwork
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.ParcelDTO
import com.delivery.sopo.networks.call.ParcelCall
import com.delivery.sopo.data.repository.local.app_password.AppPasswordRepository
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepoImpl
import com.delivery.sopo.data.repository.remote.parcel.ParcelRemoteRepository
import com.delivery.sopo.models.ResponseResult
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.views.adapter.ViewPagerAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel(private val parcelRepoImpl: ParcelRepoImpl, private val parcelManagementRepoImpl: ParcelManagementRepoImpl, private val appPasswordRepo: AppPasswordRepository): ViewModel()
{
    val mainTabVisibility = MutableLiveData<Int>()
    val errorMsg = MutableLiveData<String?>()

    // 앱 패스워드 등록 여부 TODO SplashView로 이동 예
    private val _isSetAppPassword = MutableLiveData<AppPasswordEntity?>()
    val isSetAppPassword: LiveData<AppPasswordEntity?>
        get() = _isSetAppPassword

    // 업데이트 여부
    var isInitUpdate = false

    // 유효성 및 통신 등의 결과 객체
    private var _result = MutableLiveData<ResponseResult<*>>()
    val result: LiveData<ResponseResult<*>>
        get() = _result

    val adapter = MutableLiveData<ViewPagerAdapter>()

    init
    {
        initIsSetOfSecurity()
        CoroutineScope(Dispatchers.Main).launch {
            val isUpdatableParcelStatus =
                withContext(Dispatchers.Default) { isUpdatableParcelStatus() }

            // 업데이트할 택배가 없다면 서버를 통해 갱신 처리를 하지 않는다.
            if (!isUpdatableParcelStatus)
            {
                isInitUpdate = true
                return@launch
            }
            requestOngoingParcelsAsRemote()
        }

        updateFCMToken()
    }

    private fun initIsSetOfSecurity()
    {
        viewModelScope.launch(Dispatchers.IO) {
            _isSetAppPassword.postValue(appPasswordRepo.get())
        }
    }

    fun setMainTabVisibility(visibility: Int)
    {
        mainTabVisibility.value = visibility
    }

    // 업데이트 가능한 택배가 있는지 체크 [ParcelStatusEntity - updatableStatus
    private suspend fun isUpdatableParcelStatus(): Boolean
    {
        SopoLog.d("isUpdatableParcelStatus() call")
        return parcelManagementRepoImpl.getCountForUpdatableParcel() > 0
    }

    /**
     * 이슈 : 모든 택배가 미확인 상태로 변경됨 수정이 시급
     */
    private suspend fun requestOngoingParcelsAsRemote()
    {
        SopoLog.d(msg = "requestOngoingParcelsAsRemote() call")

        val res = ParcelRemoteRepository.requestRemoteParcels()

        if (!res.result) return _result.postValue(res)

        if (res.data == null || res.data.isEmpty()) return SopoLog.d("업데이트할 택배가 없거나, 리스트 사이즈 0")

        val localParcels = withContext(Dispatchers.Default) {
            parcelRepoImpl.getLocalOngoingParcels()
        }

        // 로컬에 저장되어있는 택배가 없기 때문에 서버에서 받아온 택배 리스트를 전체 업데이트 처리해야 함
        if (localParcels.isEmpty()) return SopoLog.d("업데이트할 택배가 없거나, 리스트 사이즈 0")

        val remoteParcels = res.data.toMutableList()
        val updateParcels = mutableListOf<ParcelDTO>()
        val insertParcels = mutableListOf<ParcelDTO>()

        val remoteIterator = remoteParcels.iterator()
        val localIterator = localParcels.iterator()

        while (remoteIterator.hasNext())
        {
            val remote = remoteIterator.next()

            while (localIterator.hasNext())
            {
                val local = localIterator.next()

                if (remote.parcelId.regDt == local.parcelId.regDt && remote.parcelId.parcelUid == local.parcelId.parcelUid)
                {
                    if (remote.inquiryHash != local.inquiryHash)
                    {
                        SopoLog.d(msg = "${remote.alias}의 택배는 업데이트할 내용이 있습니다.")
                        updateParcels.add(remote)
                        // 비교 후 남는 parcel list는 insert 작업을 거친다.
                    }

                    remoteIterator.remove()
                }
            }
        }

        insertParcels.addAll(remoteParcels)

        if (insertParcels.size > 0)
        {
            SopoLog.d(
                msg = "Insert Into Room 서버에만 존재하는 데이터 ${insertParcels.size}"
            )
            // 택배 인서트

            withContext(Dispatchers.Default) {
                parcelRepoImpl.insertEntities(insertParcels)
                parcelManagementRepoImpl.insertEntities(
                    insertParcels.map(
                        ParcelMapper::parcelToParcelManagementEntity
                    )
                )
            }

        }

        if (updateParcels.size > 0)
        {
            SopoLog.d(
                msg = "Update Into Room 갱신된 데이터 ${updateParcels.size}"
            )

            withContext(Dispatchers.Default) {
                // 택배 업데이트
                parcelRepoImpl.updateEntities(updateParcels)
                parcelManagementRepoImpl.updateEntities(updateParcels.map { parcel ->
                    val pm = ParcelMapper.parcelToParcelManagementEntity(parcel)
                    pm.unidentifiedStatus = 1
                    pm
                })
            }
        }
    }

    /** Update FCM Token  **/
    private fun updateFCMToken()
    {
        SopoLog.d(msg = "updateFCMToken call()")

        FirebaseNetwork.updateFCMToken()
    }
}