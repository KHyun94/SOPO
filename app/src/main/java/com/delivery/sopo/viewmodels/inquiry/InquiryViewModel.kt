package com.delivery.sopo.viewmodels.inquiry

import android.util.Log
import androidx.lifecycle.*
import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.exceptions.APIException
import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.dto.DeleteParcelsDTO
import com.delivery.sopo.models.dto.Resource
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.local.UserRepo
import com.delivery.sopo.repository.remote.RemoteParcelRepoImpl
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import okhttp3.internal.wait
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import java.lang.Exception
import java.lang.RuntimeException

class InquiryViewModel(private val userRepo: UserRepo, private val parcelRepoImpl: RemoteParcelRepoImpl) : ViewModel()
{
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"

    private val _parcelList = MutableLiveData<Resource<out MutableList<Parcel>?>>()
    val parcelList: LiveData<Resource<out MutableList<Parcel>?>>
            get() = _parcelList

    private val _isMoreView = MutableLiveData<Boolean>()
    val isMoreView: LiveData<Boolean>
            get() = _isMoreView

    private val _isRemovable = MutableLiveData<Boolean>()
    val isRemovable: LiveData<Boolean>
            get() = _isRemovable

    private val _isSelectAll = MutableLiveData<Boolean>()
    val isSelectAll: LiveData<Boolean>
            get() = _isSelectAll

    var cntOfSelectedItem = MutableLiveData<Int>()

    private val aaa = MutableLiveData<MutableList<Parcel>>()

    private val ioScope = CoroutineScope(Dispatchers.IO)

    init{
        cntOfSelectedItem.value = 0
        _isMoreView.value = false
        _isRemovable.value = false
        _isSelectAll.value = false
        getList()
//        _parcelList.value = getList().value
//        postParcel("한성 GK993B", "kr.cjlogistics", "633505672612")
//        postParcel("토체티 듀가드 저소음 적축", "kr.cjlogistics", "633603780622")
//        postParcel("노트북 파우치", "kr.cjlogistics", "632830166566")
//        postParcel("알파스캔 27IPS86 보더리스", "kr.logen", "97911659904")
//        postParcel("ERGOTRON LX BLACK", "kr.lotte", "234886929535")
//        postParcel("LG U+ 알뜰 유심", "kr.epost", "6865402221740")
    }

    //일단 Room에 택배 정보가 있는지 확인하고 데이터가 하나도 없다면 remote 서버에 요청해서 저장된 택배 정보를 수신한다.
    private fun getList()
    {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val localParcels = parcelRepoImpl.getLocalParcels()
                if(localParcels?.size == 0){
                    Log.d(TAG, "localParcels size is zero")
                    try {
                        val parcelData = parcelRepoImpl.getRemoteParcels()
                        // local dataSource ==> insert
                        parcelRepoImpl.saveLocalParcels(parcelData as List<Parcel>)
                        _parcelList.postValue(Resource.success(parcelData))
                    }
                    catch (e: HttpException){
                        val errorLog = e.response()?.errorBody()?.charStream()
                        val apiResult = Gson().fromJson(errorLog, APIResult::class.java)
                        Log.e(TAG, "[ERROR] getParcels => $apiResult ")

                        //TODO : 로그로 남겨야함.
                        _parcelList.postValue(Resource.error(apiResult.code, null))
                    }
                }
                else{
                    _parcelList.postValue(Resource.success(localParcels))
                }
            }
        }
    }

    fun setRemovable(flag: Boolean){
        _isRemovable.value = flag
    }

    fun setMoreView(flag: Boolean){
        _isMoreView.value = flag
    }

    fun setSelectAll(flag: Boolean){
        _isSelectAll.value = flag
    }

    fun toggleMoreView(){
        _isMoreView.value?.let {
            setMoreView(!it)
        }
    }

    fun toggleSelectAll(){
        _isSelectAll.value?.let {
            setSelectAll(!it)
        }
    }

    fun cancelRemoveItem(){
        cntOfSelectedItem.value = 0
        setRemovable(false)
    }

    fun removeItem(selectedData: MutableList<ParcelId>) {
        for (selectedItem in selectedData){
            Log.d(TAG, "regDt : ${selectedItem.regDt}")
            Log.d(TAG, "uid : ${selectedItem.parcelUid}")
        }
        ioScope.launch {
            try{
                //TODO : 삭제하기가 실패했을때 로컬에서도 update 3->0으로 업데이트 시키면 안됨.
                parcelRepoImpl.deleteLocalParcelsStep1(selectedData)
                val deleteRemoteParcels = parcelRepoImpl.deleteRemoteParcels()
                if(deleteRemoteParcels.code == ResponseCode.SUCCESS.CODE){
                    Log.d(TAG, "Server side data delete SUCCESS!!")
                    parcelRepoImpl.deleteLocalParcelsStep2()
//                    _parcelList.value(Resource.success(parcelRepoImpl.getLocalParcels()))
                    _parcelList.postValue(Resource.success(parcelRepoImpl.getLocalParcels()))
                }
            }
            catch (e: HttpException){
                //TODO : 삭제하기에서 실패했을때 예외처리.
            }
        }
//        deleteParcel(selectedData)
    }

    private fun postParcel(parcelAlias: String, trackCompany: String, trackNum: String){

        ioScope.launch {
            withContext(Dispatchers.IO){
                Log.d(TAG, "email : ${userRepo.getEmail()}")
                Log.d(TAG, "password : ${userRepo.getApiPwd()}")

                NetworkManager.getPrivateParcelAPI(userRepo.getEmail(), userRepo.getApiPwd())
                    .postParcel(email = userRepo.getEmail(),
                                parcelAlias = parcelAlias,
                                trackCompany = trackCompany,
                                trackNum = trackNum)
                    .enqueue(object : Callback<APIResult<ParcelId?>>{
                        override fun onResponse(
                            call: Call<APIResult<ParcelId?>>,
                            response: Response<APIResult<ParcelId?>>
                        )
                        {
                            val code = response.code()
                            Log.d(TAG, "상태 코드 : $code")

                            when(code){
                                201 -> {
                                    Log.d(TAG,"[postParcel] onResponse : ${response.body()}")
                                }
                                400 -> {

                                }
                            }
                        }

                        override fun onFailure(call: Call<APIResult<ParcelId?>>, t: Throwable)
                        {
                            Log.d(TAG,"[postParcel] onFailure, ${t.localizedMessage}")
                        }
                    })
            }
        }
    }
}