package com.delivery.sopo.viewmodels.inquiry

import android.util.Log
import androidx.lifecycle.*
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.database.dto.TimeCountDTO
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.enums.ScreenStatus
import com.delivery.sopo.models.APIResult
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.models.parcel.ParcelId
import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.ParcelManagementRepoImpl
import com.delivery.sopo.repository.shared.UserRepo
import com.delivery.sopo.repository.ParcelRepoImpl
import com.delivery.sopo.util.fun_util.TimeUtil
import com.google.gson.Gson
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.Main
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response

class InquiryViewModel(private val userRepo: UserRepo,
                       private val parcelRepoImpl: ParcelRepoImpl,
                       private val parcelManagementRepoImpl: ParcelManagementRepoImpl) : ViewModel()
{
    private val TAG = "LOG.SOPO${this.javaClass.simpleName}"
    // 진행 중인 리스트 데이터
    private val _ongoingList by lazy{
        Transformations.map(parcelRepoImpl.getLocalOngoingParcelsLiveData()){
            ParcelMapper.parcelListToInquiryItemList(it as MutableList<Parcel>)
        }
    }
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

    // '배송중' => '배송완료' 개수
    private val _cntOfBeDelivered = parcelManagementRepoImpl.getIsDeliveredCntLiveData()
    val cntOfBeDelivered: LiveData<Int>
        get() = _cntOfBeDelivered

    // 업데이트 여부
    private val _cntOfBeUpdate = parcelManagementRepoImpl.getIsUpdateCntLiveData()
    val cntOfBeUpdate: LiveData<Int>
        get() = _cntOfBeUpdate

    private val _isForceUpdateFinish = MutableLiveData<Boolean>()
    val isForceUpdateFinish: LiveData<Boolean>
        get() = _isForceUpdateFinish

    private val ioScope = CoroutineScope(Dispatchers.IO)

    init{
        cntOfSelectedItem.value = 0
        _isForceUpdateFinish.value = false
        _isMoreView.value = false
        _isRemovable.value = false
        _isSelectAll.value = false
        _screenStatus.value = ScreenStatus.ONGOING
        sendRemoveData()
        checkIsRefreshNeed()
//        inputTestData()
    }

    private fun checkIsRefreshNeed(){
        viewModelScope.launch(Dispatchers.Default){

            // 현재 가지고 있는 아이템의 수가 0개라면 새로고침 한다.
            if(parcelRepoImpl.getOnGoingDataCnt() == 0){
                refreshOngoing()
                _isForceUpdateFinish.postValue(true)
                return@launch
            }
            else{
                val beUpdateParcelCnt = parcelManagementRepoImpl.getIsUpdateCnt()
                // View에 postValue로 값을 전달하기 전 '앱을 시작했을떄 강제 업데이트'를 해야하기 때문에 refreshOngoing()을 호출하여 강제 업데이트
                if (beUpdateParcelCnt > 0){
                    refreshOngoing()
                    _isForceUpdateFinish.postValue(true)
                    return@launch
                }
            }
            _isForceUpdateFinish.postValue(true)
        }
    }

    fun inputTestData(){
        viewModelScope.launch(Dispatchers.IO) {
            postParcel("아령", "kr.epost", "6865422872608")
            postParcel("화분", "kr.epost", "6865422872609")
            postParcel("물고기밥", "kr.epost", "6865422872610")
            postParcel("수조", "kr.epost", "6865422872611")
            postParcel("장화", "kr.epost", "6865422872612")
            postParcel("운동화", "kr.epost", "6865422872613")
            postParcel("정수기", "kr.epost", "6865422872614")
            postParcel("식탁", "kr.epost", "6865422872615")
            postParcel("액자", "kr.epost", "6865422872616")
        }
    }

    fun setCntOfDelete(value: Int){
        _cntOfDelete.value = value
    }

    fun deleteCancel(){
        viewModelScope.launch(Dispatchers.IO) {
            val cancelDataList = parcelManagementRepoImpl.getCancelIsBeDelete()
            Log.d(TAG, "삭제 취소할 데이터 : $cancelDataList")

            cancelDataList?.let { parcelMnglist ->
                parcelMnglist.forEach { it.isBeDelete = 0
                               it.auditDte = TimeUtil.getDateTime()}
                parcelManagementRepoImpl.updateEntities(parcelMnglist)

                for(parcelMng in parcelMnglist){
                    parcelRepoImpl.getLocalParcelById(parcelMng.regDt, parcelMng.parcelUid)?.let {
                        it.status = 1
                        it.auditDte = TimeUtil.getDateTime()
                        parcelRepoImpl.updateLocalOngoingParcel(it)
                    }
                }
            }
        }
    }

    private fun getMonthList(){
        viewModelScope.launch(Dispatchers.IO) {
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
        setRemovable(true) // 리스트들의 아이템들을 삭제할 수 있게 지시한다.
        setMoreView(true) // '삭제하기'를 선택했을때 '더 보기'로 숨겨져있던 아이템들도 모두 선택할 수 있어야하므로 해당 liveData를 true로 바꿔줘서 화면의 변화를 지시한다.
    }

    // 삭제하기 화면 close
    fun closeRemoveView(){
        cntOfSelectedItem.value = 0
        setRemovable(false)
        setMoreView(false)
    }

    fun refreshOngoing(){
        viewModelScope.launch {
            withContext(Dispatchers.Default) {
                Log.d(TAG, "!!!!!!! Inquiry Refreshing!!!!!!!!")
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
                        // ParcelEntity를 관리해줄 ParcelManagementEntity도 같은 ParcelId로 저장한다.
                        if(localParcelById == null){
                            parcelRepoImpl.saveLocalOngoingParcel(ParcelMapper.parcelToParcelEntity(remote))
                            parcelManagementRepoImpl.insertEntity(ParcelMapper.parcelToParcelManagementEntity(remote))
                        }
                        /*
                            서버로부터 받아온 데이터가 검색된 경우
                            => 기존에 있는 데이터라는 의미
                            => [InqueryHash를 조사해서 변화했는지 체크 및(&&) 택배의 상태가 활성화(STATUS == 1)인지 체크]
                            => 바뀐게 있다면 서버 데이터를 업데이트한다.
                         */
                        else{
                            // TODO 테스트일떄만 아래 조건문을 사용, 실제로는 위의것만 사용해야함.
//                            if(localParcelById.inqueryHash != remote.inqueryHash && localParcelById.status == 1){
                              if(localParcelById.status == 1){
                                parcelRepoImpl.updateLocalOngoingParcel(ParcelMapper.parcelToParcelEntity(remote))
                                // 업데이트 성공했으니 isBeUpdate를 0으로 다시 초기화시켜준다.
                                parcelManagementRepoImpl.initializeIsBeUpdate(remote.parcelId.regDt, remote.parcelId.parcelUid)
                            }
                        }
                    }
                }

                // 로컬에 존재하지만 새로고침하여 서버로부터 받아온 데이터 중에는 없는 데이터
                // (서버에서는 Delivered , 로컬에서는 out_for_delivery)
                // => 새로고침을 하여 GET /parcels로 받아온 데이터는 서버의 데이터베이스 기준 onGoing에 해당하는 택배들만 가져옴
                // => 따라서 로컬에서는 out_for_delivery로 배송 출발 상태(delivered 직전 상태)지만 서버에서는 delivered 상태면 새로고침 대상에서 제외됨.
                // => 이런 데이터들은 각각 GET /parcel로 요청하여 새로고침한다.
                parcelRepoImpl.getLocalOngoingParcels()?.let { localList ->
                    remoteParcels?.let { remoteList ->

                        val filteredLocalList = localList as MutableList

                        for (remoteParcel in remoteList)
                        {
                            filteredLocalList.removeIf {
                                (it.parcelId.regDt == remoteParcel.parcelId.regDt) && (it.parcelId.parcelUid == remoteParcel.parcelId.parcelUid)
                            }
                        }

                        for (parcel in filteredLocalList)
                        {
                            val remoteOngoingParcel = parcelRepoImpl.getRemoteOngoingParcel(
                                regDt = parcel.parcelId.regDt,
                                parcelUid = parcel.parcelId.parcelUid
                            )
                            val localParcelById = parcelRepoImpl.getLocalParcelById(
                                regDt = parcel.parcelId.regDt,
                                parcelUid = parcel.parcelId.parcelUid
                            )
                            if(remoteOngoingParcel != null && localParcelById != null)
                            {
                                if (remoteOngoingParcel.inqueryHash != parcel.inqueryHash)
                                {
                                    parcelRepoImpl.saveLocalOngoingParcel(localParcelById.apply { this.update(remoteOngoingParcel) })
                                    parcelManagementRepoImpl.initializeIsBeUpdate(localParcelById.regDt, localParcelById.parcelUid)
                                }
                            }
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

    // TODO : 데이터의 새로고침은 어떻게 시켜줄 것인가? , 아래로 당겨서 새로고침...?
    // 화면을 배송중 ==> 배송완료로 전환시킨다.
    fun setScreenStatusComplete(){
        _screenStatus.value = ScreenStatus.COMPLETE

        if(completeList.value == null || completeList.value?.size == 0){
            getMonthList()
        }
        // 전체 isBeDelivered를 0으로 초기화시켜준다.
        viewModelScope.launch(Dispatchers.IO){
            parcelManagementRepoImpl.updateTotalIsBeDeliveredToZero()

            //TODO TEST
            val requestRenewal2 = NetworkManager.getPrivateParcelAPI(userRepo.getEmail(), userRepo.getApiPwd()).requestRenewal2(userRepo.getEmail())
            Log.d(TAG, "requestRenewal2 : $requestRenewal2")
        }
    }

    // 데이터 삭제 로직 1단계 : Room의 status를 1==>3으로 바꾼다.
    fun removeSelectedData(selectedData: MutableList<ParcelId>) {
        ioScope.launch {
            parcelRepoImpl.deleteLocalOngoingParcels(selectedData)
            parcelManagementRepoImpl.updateIsBeDeleteToOneByParcelIdList(selectedData)
        }
    }

    // 데어터 삭제 로직 2단계 : Room의 status 3을 가진 택배들을 전부 서버로 보내서 서버 데이터 역시 삭제(1==>0)하고 최종적으로 status를 0으로 바꿔서 삭제 로직을 마무리한다.
    private fun sendRemoveData(){
        ioScope.launch {
            try{
                // 서버로 데이터를 삭제(상태 업데이트)하라고 요청
                val deleteRemoteParcels = parcelRepoImpl.deleteRemoteOngoingParcels()
                // 위 요청이 성공했다면 (삭제할 데이터가 없으면 null임)
                if(deleteRemoteParcels?.code == ResponseCode.SUCCESS.CODE) {
                    // 해당 아이템의 status(PARCEL)를 0으로 업데이트하여 삭제 처리를 마무리
                    val isBeDeleteList = parcelManagementRepoImpl.getAll()?.let { parcelMng ->
                        parcelMng.filter { it.isBeDelete == 1 }
                    }
                    isBeDeleteList?.let { list ->
                        list.forEach { it.isBeDelete = 0
                                       it.auditDte =TimeUtil.getDateTime()}
                        parcelManagementRepoImpl.updateEntities(list)
                    }

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
            Log.d(TAG, "## ==> ${parcel.deliveryStatus} && ${DeliveryStatusEnum.delivered}")
            parcel.deliveryStatus == DeliveryStatusEnum.delivered.code
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