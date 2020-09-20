package com.delivery.sopo.viewmodels.inquiry

import android.util.Log
import androidx.lifecycle.*
import com.delivery.sopo.consts.DeliveryStatus
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.enums.ScreenStatus
import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.inquiry.InquiryListData
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.shared.UserRepo
import com.delivery.sopo.repository.ParcelRepoImpl
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

class InquiryViewModel(private val userRepo: UserRepo, private val parcelRepoImpl: ParcelRepoImpl) : ViewModel()
{
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"

    // '곧 도착' 리스트 데이터
    private val _soonList = MutableLiveData<MutableList<InquiryListData>>()
    val soonList: LiveData<MutableList<InquiryListData>>
        get() = _soonList

    // '등록된 택배' 리스트 데이터
    private val _registeredList = MutableLiveData<MutableList<InquiryListData>>()
    val registerList: LiveData<MutableList<InquiryListData>>
        get() = _registeredList

    // '등록된 택배' 리스트 데이터
    private val _completeList = MutableLiveData<MutableList<InquiryListData>>()
    val completeList: LiveData<MutableList<InquiryListData>>
        get() = _completeList

    // 화면에 전체 아이템의 노출 여부
    private val _isMoreView = MutableLiveData<Boolean>()
    val isMoreView: LiveData<Boolean>
        get() = _isMoreView

    // 리스트 뷰들 아이템들을 '삭제'할 수 있는지 여부
    private val _isRemovable = MutableLiveData<Boolean>()
    val isRemovable: LiveData<Boolean>
        get() = _isRemovable

    // '전체선택' 되었는지 여부
    private val _isSelectAll = MutableLiveData<Boolean>()
    val isSelectAll: LiveData<Boolean>
        get() = _isSelectAll

    private val _screenStatus = MutableLiveData<ScreenStatus>()
    val screenStatus: LiveData<ScreenStatus>
        get() = _screenStatus

    // '삭제하기'에서 선택된 아이템의 개수
    var cntOfSelectedItem = MutableLiveData<Int>()

    private val ioScope = CoroutineScope(Dispatchers.IO)

    init{
        cntOfSelectedItem.value = 0
        _isMoreView.value = false
        _isRemovable.value = false
        _isSelectAll.value = false

        _screenStatus.value = ScreenStatus.ONGOING
        getOngoingList()
//        postParcel("타이타우", "kr.lotte", "235255141936")
//        postParcel("아베다new쿨링두피활력", "kr.lotte", "402280981874")
//        postParcel("에비앙 330ML", "kr.lotte", "307842100996")
//        postParcel("에비앙 330ML", "kr.lotte", "307842100985")
//        postParcel("아몬드 초코볼", "kr.cjlogistics", "633027402291")
//        postParcel("클렌징", "kr.cjlogistics", "381315501434")
//        postParcel("블루라운지 킥플립", "kr.cjlogistics", "   632601736701")
//        postParcel("손목 받침대", "kr.logen", "97783126932")
    }

    //일단 Room에 택배 정보가 있는지 확인하고 데이터가 하나도 없다면 remote 서버에 요청해서 저장된 택배 정보를 수신한다.
    private fun getOngoingList()
    {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                //로컬db에 데이터를 호출
                val localParcels = parcelRepoImpl.getLocalOngoingParcels()

                //로컬db에 데이터가 존재하지 않는다면
                if(localParcels?.size == 0){
                    Log.i(TAG, "로컬db에 저장된 진행중인 택배 데이터(곧 도착 + 등록된 택배)가 존재하지 않습니다.")
                    try {
                        // 서버로부터 데이터를 수신
                        val parcelData = parcelRepoImpl.getRemoteOngoingParcels()

                        if(!parcelData.isNullOrEmpty()){
                            // 데이터를 로컬db에 저장
                            parcelRepoImpl.saveLocalOngoingParcels(parcelData as List<Parcel>)

                            //수신된 데이터를 '곧 도착' 및 '등록된 택배' 리스트에 세팅
                            withContext(Main){
                                setSoonList(parcelData)
                                setRegisteredList(parcelData)
                                setCompleteList(parcelData)
                            }
                        }
                        else{
                            //수신된 데이터를 '곧 도착' 및 '등록된 택배' 리스트에 세팅
                            withContext(Main){
                                setSoonList(mutableListOf())
                                setRegisteredList(mutableListOf())
                                setCompleteList(mutableListOf())
                            }
                        }
                    }
                    catch (e: HttpException){
                        val errorLog = e.response()?.errorBody()?.charStream()
                        val apiResult = Gson().fromJson(errorLog, APIResult::class.java)
                        Log.e(TAG, "[ERROR] getParcels => $apiResult ")

                        //TODO : 로그 및 에러처리를 해야한다.
                    }
                }
                else{
                    // 로컬db에 데이터가 존재한다면 로컬db의 데이터를 '곧 도착' 및 '등록된 택배' 리스트에 데이터 세팅
                    withContext(Main){
                        setSoonList(localParcels ?: mutableListOf())
                        setRegisteredList(localParcels ?: mutableListOf())
                        setCompleteList(localParcels ?: mutableListOf())
                    }
                }
            }
        }
    }

    private fun getCompleteList(){
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val remoteCompleteParcels = parcelRepoImpl.getRemoteCompleteParcels(page = 0, inquiryDate = "202009")

                withContext(Main){
                    setCompleteList(remoteCompleteParcels ?: mutableListOf())
                }
            }
        }
    }

    // '삭제하기'에서 아이템이 전부 선택되었는지를 반환
    fun isFullySelected(selectedNum: Int): Boolean{
        return when(screenStatus.value ?: ScreenStatus.ONGOING){
            ScreenStatus.ONGOING -> {
                selectedNum == ((soonList.value?.size ?: 0) + (registerList.value?.size ?: 0))
            }
            ScreenStatus.COMPLETE -> {
                selectedNum ==  (completeList.value?.size ?: 0)
            }
        }
    }

    private fun setRemovable(flag: Boolean){
        _isRemovable.value = flag
    }

    private fun setMoreView(flag: Boolean){
        _isMoreView.value = flag
    }

    private fun setSelectAll(flag: Boolean){
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

    // 삭제하기 화면 open
    fun openRemoveView(){
        // 리스트들의 아이템들을 삭제할 수 있게 지시한다.
        setRemovable(true)

        // '삭제하기'를 선택했을때 '더 보기'로 숨겨져있던 아이템들도 모두 선택할 수 있어야하므로 해당 liveData를 true로 바꿔줘서 화면의 변화를 지시한다.
        setMoreView(true)
    }

    // 삭제하기 화면 close
    fun closeRemoveView(){
        cntOfSelectedItem.value = 0
        setRemovable(false)
        setMoreView(false)
    }

    // 새로고침
    fun refreshOngoing(){
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val remoteParcels = parcelRepoImpl.getRemoteOngoingParcels()

                remoteParcels?.let {
                    for (remote in it){
                        val localParcelById = parcelRepoImpl.getLocalParcelById(
                            remote.parcelId.regDt,
                            remote.parcelId.parcelUid
                        )
                        if(localParcelById == null){
                            parcelRepoImpl.saveLocalOngoingParcel(ParcelMapper.objectToEntity(remote))
                        }
                        else{
                            if(localParcelById.inqueryHash != remote.inqueryHash){
                                parcelRepoImpl.updateLocalOngoingParcel(ParcelMapper.objectToEntity(remote))
                            }
                        }
                    }
                    val localParcels = parcelRepoImpl.getLocalOngoingParcels()
                    withContext(Dispatchers.Main){
                        setSoonList(localParcels ?: mutableListOf())
                        setRegisteredList(localParcels ?: mutableListOf())
                    }
                }
            }
        }
    }

    fun setScreenStatusOngoing(){
        _screenStatus.value = ScreenStatus.ONGOING
    }

    fun setScreenStatusComplete(){
        _screenStatus.value = ScreenStatus.COMPLETE
        getCompleteList()
    }

    // 실질적으로 데이터를 삭제하는 로직
    fun removeSelectedData(selectedData: MutableList<ParcelId>) {
        ioScope.launch {
            try{
                //TODO : 삭제하기가 실패했을때 로컬에서도 update 3->0으로 업데이트 시키면 안됨.
                // 로컬에서 먼저 해당 선택된 아이템의 status(PARCEL 테이블의)를 3으로 바꾼다. (3으로 바꾸고 최종적으로 deleteLocalOngoingParcelsStep2에서 0으로 바뀌어야 삭제 처리 완료)
                parcelRepoImpl.deleteLocalOngoingParcelsStep1(selectedData)

                // 서버로 데이터를 삭제(상태 업데이트)하라고 요청
                val deleteRemoteParcels = parcelRepoImpl.deleteRemoteOngoingParcels()

                // 서버에서 요청이 성공했다면
                if(deleteRemoteParcels.code == ResponseCode.SUCCESS.CODE){
                    Log.d(TAG, "Server side data delete SUCCESS!!")

                    // 해당 아이템의 status(PARCEL)를 0으로 업데이트하여 삭제 처리를 마무리
                    parcelRepoImpl.deleteLocalOngoingParcelsStep2()

                    // '곧 도착' 및 '등록된 택배' 리스트 둘 다 데이터의 변화가 있었음으로 로컬db로부터 데이터를 가져와 화면을 다시 그린다.
                    val localParcels = parcelRepoImpl.getLocalOngoingParcels()
                    withContext(Main){
                        setSoonList(localParcels ?: mutableListOf())
                        setRegisteredList(localParcels ?: mutableListOf())
                    }
                }
            }
            catch (e: HttpException){
                //TODO : 삭제하기에서 실패했을때 예외처리.
            }
        }
    }

    // '곧 도착' 리스트의 데이터를 filter로 걸러 세팅한다.
    private fun setSoonList(parcelList: MutableList<Parcel>){
        _soonList.value = parcelList.filter { parcel ->
            // 리스트 중 오직 '배송출발'일 경우만 해당 adapter로 넘긴다.
            parcel.deliveryStatus == DeliveryStatus.OUT_FOR_DELIVERY
        }.map {
                filteredItem ->
            InquiryListData(parcel = filteredItem)
        } as MutableList<InquiryListData>
    }

    // '등록된 택배' 리스트의 데이터를 filter로 걸러 세팅한다.
    private fun setRegisteredList(parcelList: MutableList<Parcel>){
        _registeredList.value = parcelList.filter { parcel ->
            // 리스트 중 오직 '배송출발'일 경우만 해당 adapter로 넘긴다.
            parcel.deliveryStatus != DeliveryStatus.OUT_FOR_DELIVERY && parcel.deliveryStatus != DeliveryStatus.DELIVERED
        }.map {
                filteredItem ->
            InquiryListData(parcel = filteredItem)
        } as MutableList<InquiryListData>
    }

    // '배송완료' 리스트의 데이터를 filter로 걸러 세팅한다.
    private fun setCompleteList(parcelList: MutableList<Parcel>){
        _completeList.value = parcelList.filter { parcel ->
            // 리스트 중 오직 '배송출발'일 경우만 해당 adapter로 넘긴다.
            Log.d(TAG, "## ==> ${parcel.deliveryStatus} && ${DeliveryStatus.DELIVERED}")
            parcel.deliveryStatus == DeliveryStatus.DELIVERED
        }.map {
                filteredItem ->
            InquiryListData(parcel = filteredItem)
        } as MutableList<InquiryListData>
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
                    .enqueue(object : Callback<APIResult<ParcelId?>>
                    {
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