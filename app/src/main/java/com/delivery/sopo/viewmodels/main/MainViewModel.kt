package com.delivery.sopo.viewmodels.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.SOPOApp
import com.delivery.sopo.database.room.entity.AppPasswordEntity
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.firebase.FirebaseRepository
import com.delivery.sopo.mapper.ParcelMapper
import com.delivery.sopo.models.TestResult
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI
import com.delivery.sopo.networks.call.ParcelCall
import com.delivery.sopo.networks.call.UserCall
import com.delivery.sopo.repository.impl.AppPasswordRepoImpl
import com.delivery.sopo.repository.impl.ParcelManagementRepoImpl
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.services.network_handler.NetworkResult
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.views.adapter.ViewPagerAdapter
import com.google.firebase.iid.InstanceIdResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainViewModel(
    private val userRepoImpl : UserRepoImpl,
    private val parcelRepoImpl : ParcelRepoImpl,
    private val parcelManagementRepoImpl : ParcelManagementRepoImpl,
    private val appPasswordRepo : AppPasswordRepoImpl
) : ViewModel()
{
    val tabLayoutVisibility = MutableLiveData<Int>()
    val errorMsg = MutableLiveData<String?>()

    private val _isSetOfSecurity = MutableLiveData<AppPasswordEntity?>()
    val isSetOfSecurity : LiveData<AppPasswordEntity?>
        get() = _isSetOfSecurity

    // 업데이트 여부
    var isInitUpdate = false

    val adapter = MutableLiveData<ViewPagerAdapter>()

    init
    {
        setPrivateUserAccount()
        initIsSetOfSecurity()
        isBeUpdateParcels()
        updateFCMToken()
    }

    private fun initIsSetOfSecurity()
    {
        viewModelScope.launch(Dispatchers.IO) {
            _isSetOfSecurity.postValue(appPasswordRepo.getAppPassword())
        }
    }

    fun setTabLayoutVisibility(visibility : Int)
    {
        tabLayoutVisibility.value = visibility
    }

    // 로컬 DB - Parcel Management의 'isBeUpdate'가 1인 row들이 있는지 체크
    private fun isBeUpdateParcels()
    {
        SopoLog.d("fun isBeUpdateParcels() call")

        var cnt = 0

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                cnt = parcelManagementRepoImpl.getIsUpdateCnt()
            }

            if (cnt > 0) // 서버 DB에 업데이트된 값을 요청
            {
                SopoLog.d( msg = "Count('isBeUpdate') == ${cnt}, 서버로 요청!!!")
                requestOngoingParcelsAsRemote()
                return@launch
            }

            SopoLog.d( msg = "Count('isBeUpdate') == ${cnt}, 업데이트 사항이 없습니다!!!")
            isInitUpdate = true
        }
    }

    /**
     * 이슈 : 모든 택배가 미확인 상태로 변경됨 수정이 시급
     */
    private fun requestOngoingParcelsAsRemote()
    {
        SopoLog.d( msg = "isBeUpdateParcels 시작!!!!!!!")

        CoroutineScope(Dispatchers.Default).launch {
            when(val result = ParcelCall.getOngoingParcels())
            {
                is NetworkResult.Success ->
                {
                    val apiResult = result.data
                    val remoteParcelList = apiResult.data?:return@launch

                    if (remoteParcelList.size > 0)
                    {
                        SopoLog.d("""
                                    success to requestOngoingRemoteParcels
                                    ${remoteParcelList.joinToString()}
                                """.trimIndent())

                        // isBeUpdate가 1인 택배들의 pk 값과 inquiry_hash값을 넣을 곳
                        var localParcelList : List<Parcel?>

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
                                                msg = "REMOTE ${remote.parcelAlias}의 택배 HASH => ${remote.inquiryHash}"
                                            )
                                            SopoLog.d(
                                                msg = "LOCAL ${local.parcelAlias}의 택배 HASH => ${local.inquiryHash}"
                                            )

                                            if (remote.inquiryHash != local.inquiryHash)
                                            {
                                                SopoLog.d(
                                                    msg = "${remote.parcelAlias}의 택배는 업데이트할 내용이 있습니다."
                                                )
                                                updateParcelList.add(remote)
                                                // 비교 후 남는 parcel list는 insert 작업을 거친다.
                                                remoteIterator.remove()
                                                break
                                            }
                                            else
                                            {
                                                SopoLog.d(
                                                    msg = "${remote.parcelAlias}의 택배는 업데이트할 내용이 없습니다."
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

                                val updateManagementList =
                                    insertParcelList + updateParcelList

                                parcelManagementRepoImpl.updateEntities(updateManagementList.map { parcel ->
                                    val pm =
                                        ParcelMapper.parcelToParcelManagementEntity(parcel)
                                    pm.isUnidentified = 1
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
    }

    // network private api account setting. if it failed, try to logout and finish
    private fun setPrivateUserAccount()
    {
        if (userRepoImpl.getStatus() == 1) NetworkManager.setLogin(userRepoImpl.getEmail(), userRepoImpl.getApiPwd())
        else errorMsg.value = "로그인이 비정상적으로 이루어졌습니다.\n다시 로그인해주시길 바랍니다."
    }

    /** Update FCM Token  **/
    private fun updateFCMToken()
    {
        SopoLog.d( msg = "updateFCMToken call()")

        FirebaseRepository.updateFCMToken { result ->
            when (result)
            {
                is TestResult.SuccessResult<*> ->
                {
                    val data = result.data as InstanceIdResult
                    val token = data.token

                    SopoLog.d( msg = "FCM ===> $token")

                    CoroutineScope(Dispatchers.IO).launch {

                        when (val result = UserCall.updateFCMToken(fcmToken = token))
                        {
                            is NetworkResult.Success ->
                            {
                                SopoLog.d( msg = "Success To Update FCM Token ${result.data.message}")
                            }
                            is NetworkResult.Error ->
                            {
                                SopoLog.d( msg = "Fail To Update FCM Token ${result.exception.message}")
                            }
                        }
                    }
                }
                is TestResult.ErrorResult<*> ->
                {
                    SopoLog.e( msg = "Fail To Update FCM Token ${result.errorMsg}", e = result.e)
                }
            }
        }


    }
}