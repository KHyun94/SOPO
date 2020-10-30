package com.delivery.sopo.viewmodels.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.database.room.entity.AppPasswordEntity
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.impl.AppPasswordRepoImpl
import com.delivery.sopo.repository.impl.ParcelManagementRepoImpl
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainViewModel(
    private val userRepoImpl: UserRepoImpl,
    private val parcelRepoImpl: ParcelRepoImpl,
    private val parcelManagementRepoImpl: ParcelManagementRepoImpl,
    private val appPasswordRepo: AppPasswordRepoImpl
) : ViewModel()
{

    lateinit var registeredParcelCnt : LiveData<Int>
    val tabLayoutVisibility = MutableLiveData<Int>()
    val errorMsg = MutableLiveData<String?>()

    private val _isSetOfSecurity = MutableLiveData<AppPasswordEntity?>()
    val isSetOfSecurity: LiveData<AppPasswordEntity?>
        get() = _isSetOfSecurity

    // 업데이트 여부
    private val _cntOfBeUpdate = parcelManagementRepoImpl.getIsUpdateCntLiveData()
    val cntOfBeUpdate: LiveData<Int>
        get() = _cntOfBeUpdate

    init
    {
        setPrivateUserAccount()
        setRegisteredParcelCnt()
    }

    fun setRegisteredParcelCnt(){
        registeredParcelCnt = parcelRepoImpl.getOngoingDataCntLiveData()
        initIsSetOfSecurity()
    }

    private fun initIsSetOfSecurity(){
         viewModelScope.launch(Dispatchers.IO){
             _isSetOfSecurity.postValue(appPasswordRepo.getAppPassword())
         }
    }

    fun setTabLayoutVisibility(visibility: Int)
    {
        tabLayoutVisibility.value = visibility
    }

    // network private api account setting. if it failed, try to logout and finish
    private fun setPrivateUserAccount()
    {
        if(userRepoImpl.getStatus() == 1)
            NetworkManager.initPrivateApi(userRepoImpl.getEmail(), userRepoImpl.getApiPwd())
        else
            errorMsg.value = "로그인이 비정상적으로 이루어졌습니다.\n다시 로그인해주시길 바랍니다."
    }
}