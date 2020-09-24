package com.delivery.sopo.viewmodels.inquiry

import android.util.Log
import androidx.lifecycle.*
import com.delivery.sopo.consts.DeliveryStatus
import com.delivery.sopo.database.dto.TimeCountDTO
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.enums.ScreenStatus
import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.mapper.ParcelMapper
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

    // 진행 중인 리스트 데이터
    private val _ongoingList = MutableLiveData<MutableList<InquiryListItem>>()
    val ongoingList: LiveData<MutableList<InquiryListItem>>
        get() = _ongoingList

    //  배송완료 리스트 데이터
    private val _completeList = MutableLiveData<MutableList<InquiryListItem>>()
    val completeList: LiveData<MutableList<InquiryListItem>>
        get() = _completeList

    // 배송완료 조회 가능한 '년월' 리스트 데이터
    private val _monthList = MutableLiveData<MutableList<TimeCountDTO>>()
    val monthList: LiveData<MutableList<TimeCountDTO>>
        get() = _monthList

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

    // '프로그래스 바' 표출 여부
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    // '배송 중' 또는 '배송완료' 화면 선택의 기준
    private val _screenStatus = MutableLiveData<ScreenStatus>()
    val screenStatus: LiveData<ScreenStatus>
        get() = _screenStatus

    // '삭제하기'에서 선택된 아이템의 개수
    var cntOfSelectedItem = MutableLiveData<Int>()

    private val _cntOfDelete = MutableLiveData<Int>()
    val cntOfDelete: LiveData<Int>
        get() = _cntOfDelete


    private val ioScope = CoroutineScope(Dispatchers.IO)

    init{
        cntOfSelectedItem.value = 0
        _isMoreView.value = false
        _isRemovable.value = false
        _isSelectAll.value = false

        _screenStatus.value = ScreenStatus.ONGOING
        getOngoingList()
        sendRemoveData()
//        postParcel("타이타우", "kr.lotte", "235255141936")
//        postParcel("아베다new쿨링두피활력", "kr.lotte", "402280981874")
//        postParcel("에비앙 330ML", "kr.lotte", "307842100996")
//        postParcel("에비앙 330ML", "kr.lotte", "307842100985")
//        postParcel("아몬드 초코볼", "kr.cjlogistics", "633027402291")
//        postParcel("클렌징", "kr.cjlogistics", "381315501434")
//        postParcel("블루라운지 킥플립", "kr.cjlogistics", "   632601736701")
//        postParcel("손목 받침대", "kr.logen", "97783126932")
    }

    fun setCntOfDelete(value: Int){
        _cntOfDelete.value = value
    }

    fun deleteCancel(){
        viewModelScope.launch(Dispatchers.IO) {
            val localBeDeleteCanceledParcel = parcelRepoImpl.getLocalBeDeleteCanceledParcel()
            Log.d(TAG, "삭제 취소할 데이터 : $localBeDeleteCanceledParcel")
            localBeDeleteCanceledParcel?.let {
                list ->
                    list.forEach { it.status = 1 }
                    parcelRepoImpl.updateLocalOngoingParcels(list)
            }
            parcelRepoImpl.getLocalOngoingParcels()?.let {
                _ongoingList.postValue(ParcelMapper.parcelListToInquiryItemList(it))
            }
        }
    }

    private fun getMonthList(){
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                _isLoading.postValue(true)
                val remoteMonthList = parcelRepoImpl.getRemoteMonthList()
                remoteMonthList?.let { list ->
                    for(timeCnt in list){
                        Log.d(TAG, "${timeCnt.time} && ${timeCnt.count}")
                    }
                }
                _monthList.postValue(remoteMonthList)
                _isLoading.postValue(false)
            }
        }
    }

    //일단 Room에 택배 정보가 있는지 확인하고 데이터가 하나도 없다면 remote 서버에 요청해서 저장된 택배 정보를 수신한다.
    private fun getOngoingList()
    {
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                //로컬db에 데이터를 호출sendRemoveData
                val localParcels = parcelRepoImpl.getLocalOngoingParcels()

                //로컬db에 데이터가 존재하지 않는다면
                if(localParcels?.size == 0){
                    Log.i(TAG, "로컬db에 저장된 진행중인 택배 데이터(곧 도착 + 등록된 택배)가 존재하지 않습니다.")
                    try {
                        // 서버로부터 데이터를 수신
                        val parcelData = parcelRepoImpl.getRemoteOngoingParcels()

                        // 서버로부터 받아온 데이터가 빈 값이 아니라면
                        if(!parcelData.isNullOrEmpty()){
                            // 데이터를 로컬db에 저장
                            parcelRepoImpl.saveLocalOngoingParcels(parcelData as List<Parcel>)

                            // 저장된 데이터를 리스트에 표출하기 위하여 Repo로부터 다시 호출
                            val localOngoingParcels = parcelRepoImpl.getLocalOngoingParcels()
                            // 서버로부터도 받아온 값이 없다면 Empty View를 화면에 노출시킨다.
                            if(localOngoingParcels.isNullOrEmpty()){
                                _ongoingList.postValue(mutableListOf())
                            }
                            // 서버로부터 받아온 값이 있다면(로컬로부터 다시 호출됨)
                            else{
                                //수신된 데이터를 '곧 도착' 및 '등록된 택배' 리스트에 세팅
                                _ongoingList.postValue(ParcelMapper.parcelListToInquiryItemList(localOngoingParcels))
                            }
                        }
                        // 서버로부터 받아온 데이터가 빈 값일 경
                        else{
                            _ongoingList.postValue(mutableListOf())
                        }
                    }
                    catch (e: HttpException){
                        val errorLog = e.response()?.errorBody()?.charStream()
                        val apiResult = Gson().fromJson(errorLog, APIResult::class.java)
                        Log.e(TAG, "[ERROR] getParcels => $apiResult ")

                        //TODO : 로그 및 에러처리를 해야한다.
                    }
                }
                //로컬 db에 데이터가 존재하는 경우
                else{
                    // 한번 더 비었는지 체크
                    if(localParcels.isNullOrEmpty()){
                        _ongoingList.postValue(mutableListOf())
                    }
                    // 로컬db에 데이터가 존재한다면 로컬db의 데이터를 '곧 도착' 및 '등록된 택배' 리스트에 데이터 세팅
                    else {
                        _ongoingList.postValue(ParcelMapper.parcelListToInquiryItemList(localParcels))
                    }
                }
            }
        }
    }

    fun getCompleteList(inquiryDate: String){
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val remoteCompleteParcels = parcelRepoImpl.getRemoteCompleteParcels(page = 0, inquiryDate = inquiryDate)

                remoteCompleteParcels?.sortByDescending { it.arrivalDte }
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
                selectedNum == (ongoingList.value?.size ?: 0)
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

    //TODO : 서버에는 아직 데이터 삭제(status == 0)이 반영 안되어있고 app은 status 3인 경우,
    //TODO : 서버에서 가져온 데이터가 업데이트 되었을때(inquiry hash에 변화가 생김) 해당 아이템 업데이트 여부 체크
    // 새로고침
    fun refreshOngoing(){
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                val remoteParcels = parcelRepoImpl.getRemoteOngoingParcels()

                // 서버로부터 데이터를 받아온 값이 널이 아니라면
                remoteParcels?.let {

                    // 서버로부터 받아온 데이터(ParcelId로 검색)로 로컬 db에 검색이 되는지 확인한다.
                    for (remote in it){
                        val localParcelById = parcelRepoImpl.getLocalParcelById(
                            remote.parcelId.regDt,
                            remote.parcelId.parcelUid
                        )
                        // 서버로부터 받아온 데이터가 검색되지 않았을 경우 => 새로운 데이터라는 의미 => 로컬에 저장한다.
                        if(localParcelById == null){
                            parcelRepoImpl.saveLocalOngoingParcel(ParcelMapper.parcelToEntity(remote))
                        }
                        /*
                            서버로부터 받앙온 데이터가 검색된 경우
                            => 기존에 있는 데이터라는 의미
                            => [InqueryHash를 조사해서 변화했는지 체크 및(&&) 택배의 상태가 활성화(STATUS == 1)인지 체크]
                            => 바뀐게 있다면 서버 데이터를 업데이트한다.
                         */
                        else{
                            if(localParcelById.inqueryHash != remote.inqueryHash && localParcelById.status == 1){
                                parcelRepoImpl.updateLocalOngoingParcel(ParcelMapper.parcelToEntity(remote))
                            }
                        }
                    }
                    // 로컬에 반영된 데이터(서버)를 다시 불러와서 화면에 반영한다.
                    val localParcels = parcelRepoImpl.getLocalOngoingParcels()
                    // 데이터가 비었다면 Empty 화면을 표출시킨다.
                    if(localParcels.isNullOrEmpty()){
//                        _isOngoingEmptyView.postValue(true)
                    }
                    // 로컬db에 데이터가 존재한다면 로컬db의 데이터를 '곧 도착' 및 '등록된 택배' 리스트에 데이터 세팅
                    else{
                        withContext(Main){
//                            setSoonList(localParcels)
//                            setRegisteredList(localParcels)
                            _ongoingList.value = localParcels.map {
                                    parcel ->
                                InquiryListItem(parcel = parcel)
                            } as MutableList<InquiryListItem>
                        }
                    }
                }
            }
        }
    }

    // 화면을 배송완료 ==> 배송중으로 전환시킨다.
    fun setScreenStatusOngoing(){
        _screenStatus.value = ScreenStatus.ONGOING
    }

    // TODO : 데이터의 새로고침은 어떻게 시켜줄 것인가?
    // 화면을 배송중 ==> 배송완료로 전환시킨다.
    fun setScreenStatusComplete(){
        _screenStatus.value = ScreenStatus.COMPLETE

        if(completeList.value == null || completeList.value?.size == 0){
            getMonthList()
        }
    }

    // 데이터 삭제 로직 1단계 : Room의 status를 1==>3으로 바꾼다.
    fun removeSelectedData(selectedData: MutableList<ParcelId>) {
        ioScope.launch {
            parcelRepoImpl.deleteLocalOngoingParcelsStep1(selectedData)

            // '곧 도착' 및 '등록된 택배' 리스트 둘 다 데이터의 변화가 있었음으로 로컬db로부터 데이터를 가져와 화면을 다시 그린다.
            val localParcels = parcelRepoImpl.getLocalOngoingParcels()
            withContext(Main){
                _ongoingList.postValue(ParcelMapper.parcelListToInquiryItemList(localParcels ?: mutableListOf()))
            }
        }
    }

    // 데어터 삭제 로직 2단계 : Room의 status 3을 가진 택배들을 전부 서버로 보내서 서버 데이터 역시 삭제(1==>0)하고 최종적으로 status를 0으로 바꿔서 삭제 로직을 마무리한다.
    private fun sendRemoveData(){
        ioScope.launch {
            try{
                // 서버로 데이터를 삭제(상태 업데이트)하라고 요청
                val deleteRemoteParcels = parcelRepoImpl.deleteRemoteOngoingParcels()
                // 위 요청이 성공했다면
                if(deleteRemoteParcels.code == ResponseCode.SUCCESS.CODE) {
                    // 해당 아이템의 status(PARCEL)를 0으로 업데이트하여 삭제 처리를 마무리
                    parcelRepoImpl.deleteLocalOngoingParcelsStep2()
                    Log.i(TAG, "SUCCESS to send delete data")
                }
            }
            catch (e: HttpException){
                //TODO : 삭제하기에서 실패했을때 예외처리.
                val errorLog = e.response()?.errorBody()?.charStream()
                val apiResult = Gson().fromJson(errorLog, APIResult::class.java)
                Log.i(TAG, "Fail to send delete data : ${apiResult.message}") // 실패 로그
            }
        }
    }

    // '배송완료' 리스트의 데이터를 filter로 걸러 세팅한다.
    private fun setCompleteList(parcelList: MutableList<Parcel>){
        _completeList.value = parcelList.filter { parcel ->
            // 리스트 중 오직 '배송출발'일 경우만 해당 adapter로 넘긴다.
            Log.d(TAG, "## ==> ${parcel.deliveryStatus} && ${DeliveryStatus.DELIVERED}")
            parcel.deliveryStatus == DeliveryStatus.DELIVERED
        }.map {
                filteredItem ->
            InquiryListItem(parcel = filteredItem)
        } as MutableList<InquiryListItem>
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