package com.delivery.sopo.viewmodels.main

import androidx.lifecycle.*
import com.delivery.sopo.database.room.entity.AppPasswordEntity
import com.delivery.sopo.database.room.entity.ParcelEntity
import com.delivery.sopo.enums.ResponseCodeEnum
import com.delivery.sopo.mapper.ParcelMapper
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.networks.api.ParcelAPI
import com.delivery.sopo.repository.impl.AppPasswordRepoImpl
import com.delivery.sopo.repository.impl.ParcelManagementRepoImpl
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.livedates.SingleLiveEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MainViewModel(
    private val userRepoImpl: UserRepoImpl,
    private val parcelRepoImpl: ParcelRepoImpl,
    private val parcelManagementRepoImpl: ParcelManagementRepoImpl,
    private val appPasswordRepo: AppPasswordRepoImpl
) : ViewModel()
{
    val tabLayoutVisibility = MutableLiveData<Int>()
    val errorMsg = MutableLiveData<String?>()

    private val _isSetOfSecurity = MutableLiveData<AppPasswordEntity?>()
    val isSetOfSecurity: LiveData<AppPasswordEntity?>
        get() = _isSetOfSecurity

    // 업데이트 여부
    var isInitUpdate = false
    private val _cntOfBeUpdate: LiveData<Int> = parcelManagementRepoImpl.getIsUpdateCntLiveData()
    val cntOfBeUpdate : LiveData<Int>
    get() = _cntOfBeUpdate

    init
    {
        setPrivateUserAccount()
        initIsSetOfSecurity()
        isBeUpdateParcels()
    }

    private fun initIsSetOfSecurity()
    {
        viewModelScope.launch(Dispatchers.IO) {
            _isSetOfSecurity.postValue(appPasswordRepo.getAppPassword())
        }
    }

    fun setTabLayoutVisibility(visibility: Int)
    {
        tabLayoutVisibility.value = visibility
    }

    // 로컬 DB - Parcel Management의 'isBeUpdate'가 1인 row들이 있는지 체크
    fun isBeUpdateParcels()
    {
        SopoLog.d("isBeUpdateParcels 시작!!!!!")
        var cnt = 0

        CoroutineScope(Dispatchers.Main).launch {
            withContext(Dispatchers.Default) {
                cnt = parcelManagementRepoImpl.getIsUpdateCnt()
            }

            if (cnt > 0) // 서버 DB에 업데이트된 값을 요청
            {
                SopoLog.d("Count('isBeUpdate') == ${cnt}, 서버로 요청!!!")
                requestOngoingRemoteParcels()
            }
            else
            {
                SopoLog.d("Count('isBeUpdate') == ${cnt}, 업데이트 사항이 없습니다!!!")
                isInitUpdate = true
            }
        }
    }

    // todo TEST 해봐야합니다.
    private fun requestOngoingRemoteParcels()
    {
        NetworkManager.privateRetro.create(ParcelAPI::class.java)
            .getParcelsOngoingTmp(userRepoImpl.getEmail())
            .enqueue(object : Callback<APIResult<MutableList<Parcel>?>?>
            {
                override fun onFailure(call: Call<APIResult<MutableList<Parcel>?>?>, t: Throwable)
                {
                    SopoLog.e("업데이트 요청 실패", t)
                    isInitUpdate = true
                }

                override fun onResponse(
                    call: Call<APIResult<MutableList<Parcel>?>?>,
                    response: Response<APIResult<MutableList<Parcel>?>?>
                )
                {
                    when (response.code())
                    {
                        ResponseCodeEnum.SUCCESS.HTTP_STATUS ->
                        {
                            val result = response.body()
                            val remoteParcelList = result?.data

                            SopoLog.d("서버에서 가져온 진행 중인 택배 리스트 $result")

                            if(remoteParcelList != null)
                            {
                                // isBeUpdate가 1인 택배들의 pk 값과 inquiry_hash값을 넣을 곳
                                var localParcelList: List<ParcelEntity?>? = null

                                // 로컬과 서버 간 inquiry hash 값을 비교했을 때 다를 경우 넣는 곳
                                val updateParcelList =  mutableListOf<Parcel>()

                                // 로컬에 없는 parcel data를 넣는 곳
                                var insertParcelList= mutableListOf<Parcel>()

                                CoroutineScope(Dispatchers.Main).launch {
                                    withContext(Dispatchers.Default) {
                                        localParcelList = parcelRepoImpl.getUpdatableInquiryHash()
                                    }

                                    if(localParcelList == null && localParcelList!!.isEmpty())
                                    {
                                        insertParcelList = remoteParcelList
                                    }
                                    else
                                    {
                                        for (remote in remoteParcelList)
                                        {
                                            for (local in localParcelList!!)
                                            {
                                                if(remote.parcelId.regDt == local!!.regDt && remote.parcelId.parcelUid == local.parcelUid)
                                                {
                                                    if(remote.inqueryHash != local.inqueryHash)
                                                    {
                                                        SopoLog.d("${remote.parcelAlias}의 택배는 업데이트할 내용이 있습니다.")
                                                        updateParcelList.add(remote)
                                                        // 비교 후 남는 parcel list는 insert 작업을 거친다.
                                                        remoteParcelList.remove(remote)
                                                        break
                                                    }
                                                    else
                                                    {
                                                        SopoLog.d("${remote.parcelAlias}의 택배는 업데이트할 내용이 없습니다.")
                                                        // 비교 후 남는 parcel list는 insert 작업을 거친다.
                                                        remoteParcelList.remove(remote)
                                                    }
                                                }
                                            }
                                        }

                                        insertParcelList = remoteParcelList
                                    }

                                    withContext(Dispatchers.Default){
                                        if(insertParcelList.size > 0)
                                        {
                                            // 택배 인서트
                                            parcelRepoImpl.insertEntities(insertParcelList)
                                            parcelManagementRepoImpl.insertEntities(insertParcelList.map(ParcelMapper::parcelToParcelManagementEntity))
                                        }

                                        if(updateParcelList.size > 0)
                                        {
                                            // 택배 업데이트
                                            parcelRepoImpl.updateEntities(updateParcelList)
                                        }
                                    }
                                    isInitUpdate = true
                                }

                            }

                        }
                        else ->
                        {
                            isInitUpdate = true
                        }
                    }
                }

            })
    }

    // network private api account setting. if it failed, try to logout and finish
    private fun setPrivateUserAccount()
    {
        if (userRepoImpl.getStatus() == 1)
            NetworkManager.initPrivateApi(userRepoImpl.getEmail(), userRepoImpl.getApiPwd())
        else
            errorMsg.value = "로그인이 비정상적으로 이루어졌습니다.\n다시 로그인해주시길 바랍니다."
    }
}