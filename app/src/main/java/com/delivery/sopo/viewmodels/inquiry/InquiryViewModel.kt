package com.delivery.sopo.viewmodels.inquiry

import android.util.Log
import androidx.lifecycle.*
import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.dto.DeleteParcelsDTO
import com.delivery.sopo.models.inquiry.InquiryListData
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.UserRepo
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.RuntimeException

class InquiryViewModel(private val userRepo: UserRepo) : ViewModel()
{
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"
    val parcelList: MutableLiveData<MutableList<Parcel>?> = MutableLiveData()
    val isMoreView = MutableLiveData<Boolean>()
    val isRemovable = MutableLiveData<Boolean>()
    val isSelectAll = MutableLiveData<Boolean>()
    var cntOfSelectedItem = MutableLiveData<Int>()


    init{
        getAllParcelList()
        cntOfSelectedItem.value = 0
        isMoreView.value = false
        isRemovable.value = false
        isSelectAll.value = false
//        postParcel("한성 GK993B", "kr.cjlogistics", "633505672612")
//        postParcel("토체티 듀가드 저소음 적축", "kr.cjlogistics", "633603780622")
//        postParcel("노트북 파우치", "kr.cjlogistics", "632830166566")
//        postParcel("알파스캔 27IPS86 보더리스", "kr.logen", "97911659904")
//        postParcel("ERGOTRON LX BLACK", "kr.lotte", "234886929535")
//        postParcel("LG U+ 알뜰 유심", "kr.epost", "6865402221740")
    }

    fun setRemovable(flag: Boolean){
        isRemovable.value = flag
    }

    fun setMoreView(flag: Boolean){
        isMoreView.value = flag
    }

    fun setSelectAll(flag: Boolean){
        isSelectAll.value = flag
    }


    fun toggleMoreView(){
        isMoreView.value?.let {
            setMoreView(!it)
        }
    }

    fun toggleSelectAll(){
        isSelectAll.value?.let {
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
        deleteParcel(selectedData)
    }

    private fun getAllParcelList(){

        val ioScope = CoroutineScope(Dispatchers.IO)
        ioScope.launch {
            withContext(Dispatchers.IO){
//                parcelList.value = getParcels().data

                Log.d(TAG, "email : ${userRepo.getEmail()}")
                Log.d(TAG, "password : ${userRepo.getApiPwd()}")

                NetworkManager.getPrivateParcelAPI(userRepo.getEmail(), userRepo.getApiPwd())
                    .getParcelsAsync(email = userRepo.getEmail())
                    .enqueue(object : Callback<APIResult<MutableList<Parcel>?>>{
                        override fun onResponse(
                            call: Call<APIResult<MutableList<Parcel>?>>,
                            response: Response<APIResult<MutableList<Parcel>?>>
                        )
                        {
                            val code = response.code()
                            Log.d(TAG, "상태 코드 : $code")

                            when(code){
                                200 -> {
                                    val apiResult = response.body() ?: throw RuntimeException("[getAllParcelList] ==> 해당 메서드에서 http status code가 200인데도 불구하고 APIResult가 NULL임")

                                    Log.d(TAG, "response : $apiResult")

                                    val parcels = apiResult.data
                                    if(parcels.isNullOrEmpty()){
                                        Log.d(TAG,"아직 등록된 택배가 없습니다.")
                                    }
                                    else{
                                        for(parcel in parcels){
                                            Log.d(TAG, "택배명 : ${parcel.parcelAlias}")
                                            Log.d(TAG, "택배 이동 상태 : ${parcel.deliveryStatus}")
                                        }
                                        parcelList.value = parcels
                                    }
                                }
                                400 -> {

                                }
                            }
                        }

                        override fun onFailure(call: Call<APIResult<MutableList<Parcel>?>>, t: Throwable)
                        {
                            Log.d(TAG,"[getAllParcelList] ==> onFailure, ${t.localizedMessage}")
                        }
                    })
            }
        }
    }

    private fun deleteParcel(list: MutableList<ParcelId>){
        NetworkManager.getPrivateParcelAPI(userRepo.getEmail(), userRepo.getApiPwd())
            .deleteParcels(email = userRepo.getEmail(),
                            parcelIds = DeleteParcelsDTO(list))
                                .enqueue(object : Callback<APIResult<String?>>
                            {
                                override fun onResponse(
                                    call: Call<APIResult<String?>>,
                                    response: Response<APIResult<String?>>
                                )
                                {
                                    val code = response.code()
                                    Log.d(TAG, "상태 코드 : $code")

                                    when(code){
                                        200 -> {

                                        }
                                        400 -> {
                                            Log.d(TAG, "#### : $code")
                                        }
                                    }
                                }

                                override fun onFailure(call: Call<APIResult<String?>>, t: Throwable) {
                                    Log.d(TAG,"[deleteParcel] ==> onFailure, ${t.localizedMessage}")
                                }
                            })
    }

    private fun postParcel(parcelAlias: String, trackCompany: String, trackNum: String){

        val ioScope = CoroutineScope(Dispatchers.IO)
        ioScope.launch {
            withContext(Dispatchers.IO){
                Log.d(TAG, "email : ${userRepo.getEmail()}")
                Log.d(TAG, "password : ${userRepo.getApiPwd()}")

                NetworkManager.getPrivateParcelAPI(userRepo.getEmail(), userRepo.getApiPwd())
                    .registerParcel(email = userRepo.getEmail(),
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