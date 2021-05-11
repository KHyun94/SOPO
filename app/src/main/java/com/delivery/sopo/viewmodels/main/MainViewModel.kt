package com.delivery.sopo.viewmodels.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.data.repository.database.room.entity.AppPasswordEntity
import com.delivery.sopo.firebase.FirebaseNetwork
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.networks.call.ParcelCall
import com.delivery.sopo.data.repository.local.app_password.AppPasswordRepository
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepoImpl
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

    val adapter = MutableLiveData<ViewPagerAdapter>()

    init
    {
        initIsSetOfSecurity()
        CoroutineScope(Dispatchers.Main).launch {
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

        val isUpdatableParcelStatus = withContext(Dispatchers.Default){ isUpdatableParcelStatus() }

        if(!isUpdatableParcelStatus)
        {
            isInitUpdate = true
            return
        }

        when (val result = ParcelCall.getOngoingParcels())
        {
            is NetworkResult.Success ->
            {
                val apiResult = result.data
                val remoteParcelList = apiResult.data ?: return

                if (remoteParcelList.size > 0)
                {
                    SopoLog.d(
                        """
                                    success to requestOngoingRemoteParcels
                                    ${remoteParcelList.joinToString()}
                                """.trimIndent()
                    )

                    // isBeUpdate가 1인 택배들의 pk 값과 inquiry_hash값을 넣을 곳
                    var localParcelList: List<Parcel?>

                    // 로컬과 서버 간 inquiry hash 값을 비교했을 때 다를 경우 넣는 곳
                    val updateParcelList = mutableListOf<Parcel>()

                    // 로컬에 없는 parcel data를 넣는 곳
                    var insertParcelList = mutableListOf<Parcel>()

                    CoroutineScope(Dispatchers.Default).launch {
                        localParcelList = parcelRepoImpl.getLocalOngoingParcels()
                    }

                    CoroutineScope(Dispatchers.Main).launch {
                        withContext(Dispatchers.Default) {
                            localParcelList = parcelRepoImpl.getLocalOngoingParcels()
                        }

                        if (localParcelList.isEmpty())
                        {
                            insertParcelList = remoteParcelList
                        }
                        else
                        {
                            val remoteIterator = remoteParcelList.iterator()
                            val localIterator = localParcelList.iterator()

                            while (remoteIterator.hasNext())
                            {
                                val remote = remoteIterator.next()

                                while (localIterator.hasNext())
                                {
                                    val local = localIterator.next()

                                    if (remote.parcelId.regDt == local!!.parcelId.regDt && remote.parcelId.parcelUid == local.parcelId.parcelUid)
                                    {
                                        SopoLog.d(
                                            msg = "REMOTE ${remote.alias}의 택배 HASH => ${remote.inquiryHash}"
                                        )
                                        SopoLog.d(
                                            msg = "LOCAL ${local.alias}의 택배 HASH => ${local.inquiryHash}"
                                        )

                                        if (remote.inquiryHash != local.inquiryHash)
                                        {
                                            SopoLog.d(
                                                msg = "${remote.alias}의 택배는 업데이트할 내용이 있습니다."
                                            )
                                            updateParcelList.add(remote)
                                            // 비교 후 남는 parcel list는 insert 작업을 거친다.
                                            remoteIterator.remove()
                                            break
                                        }
                                        else
                                        {
                                            SopoLog.d(
                                                msg = "${remote.alias}의 택배는 업데이트할 내용이 없습니다."
                                            )
                                            // 비교 후 남는 parcel list는 insert 작업을 거친다.
                                            remoteIterator.remove()
                                        }
                                    }
                                }
                            }

                            insertParcelList = remoteParcelList
                        }

                        withContext(Dispatchers.Default) {
                            if (insertParcelList.size > 0)
                            {
                                SopoLog.d(
                                    msg = "Insert Into Room 서버에만 존재하는 데이터 ${insertParcelList.size}"
                                )
                                // 택배 인서트
                                parcelRepoImpl.insertEntities(insertParcelList)
                                parcelManagementRepoImpl.insertEntities(
                                    insertParcelList.map(
                                        ParcelMapper::parcelToParcelManagementEntity
                                    )
                                )
                            }

                            if (updateParcelList.size > 0)
                            {
                                SopoLog.d(
                                    msg = "Update Into Room 갱신된 데이터 ${updateParcelList.size}"
                                )
                                // 택배 업데이트
                                parcelRepoImpl.updateEntities(updateParcelList)
                            }

                            val updateManagementList = insertParcelList + updateParcelList

                            parcelManagementRepoImpl.updateEntities(updateManagementList.map { parcel ->
                                val pm = ParcelMapper.parcelToParcelManagementEntity(parcel)
                                pm.unidentifiedStatus = 1
                                pm
                            })

                        }
                        isInitUpdate = true
                    }

                }

            }
            is NetworkResult.Error ->
            {
                val error = result.exception
                SopoLog.e("Fail to request ongoing parcel as remote", error)
                isInitUpdate = true
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