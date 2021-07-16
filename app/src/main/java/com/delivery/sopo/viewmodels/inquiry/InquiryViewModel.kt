package com.delivery.sopo.viewmodels.inquiry

import androidx.lifecycle.*
import com.delivery.sopo.data.repository.database.room.entity.ParcelCntInfoEntity
import com.delivery.sopo.enums.ResponseCode
import com.delivery.sopo.enums.ScreenStatusEnum
import com.delivery.sopo.models.mapper.MenuMapper
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.mapper.TimeCountMapper
import com.delivery.sopo.models.api.APIResult
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.inquiry.PagingManagement
import com.delivery.sopo.models.parcel.ParcelDTO
import com.delivery.sopo.networks.call.ParcelCall
import com.delivery.sopo.networks.dto.TimeCountDTO
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.data.repository.local.repository.TimeCountRepoImpl
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.parcel.ParcelUseCase
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.models.UpdateAliasRequest
import com.delivery.sopo.util.SopoLog
import com.delivery.sopo.util.TimeUtil
import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.HttpException
import java.util.*
import kotlin.Comparator

class InquiryViewModel(private val userLocalRepository: UserLocalRepository, private val parcelRepository: ParcelRepository, private val parcelManagementRepoImpl: ParcelManagementRepoImpl, private val timeCountRepoImpl: TimeCountRepoImpl):
        ViewModel()
{
    private var _ongoingList = Transformations.map(parcelRepository.getLocalOngoingParcelsAsLiveData()) { parcelList ->

        val list: MutableList<InquiryListItem> = ParcelMapper.parcelListToInquiryItemList(parcelList)

        sortByDeliveryStatus(list).toMutableList()
/*
        list.apply {

            val sortedList = mutableListOf<InquiryListItem>()

            val elseList = list.asSequence().filter { item ->

                if(item.parcelDTO.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE)
                {
                    sortedList.add(item)
                }

                item.parcelDTO.deliveryStatus != DeliveryStatusEnum.DELIVERED.CODE
            }.filter { item ->
                if(item.parcelDTO.deliveryStatus == DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE)
                {
                    sortedList.add(item)
                }

                item.parcelDTO.deliveryStatus != DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE
            }.filter { item ->
                if(item.parcelDTO.deliveryStatus == DeliveryStatusEnum.IN_TRANSIT.CODE)
                {
                    sortedList.add(item)
                }

                item.parcelDTO.deliveryStatus != DeliveryStatusEnum.IN_TRANSIT.CODE
            }.filter { item ->
                if(item.parcelDTO.deliveryStatus == DeliveryStatusEnum.AT_PICKUP.CODE)
                {
                    sortedList.add(item)
                }

                item.parcelDTO.deliveryStatus != DeliveryStatusEnum.AT_PICKUP.CODE
            }.filter { item ->
                if(item.parcelDTO.deliveryStatus == DeliveryStatusEnum.INFORMATION_RECEIVED.CODE)
                {
                    sortedList.add(item)
                }

                item.parcelDTO.deliveryStatus != DeliveryStatusEnum.INFORMATION_RECEIVED.CODE
            }.filter { item ->
                if(item.parcelDTO.deliveryStatus == DeliveryStatusEnum.NOT_REGISTER.CODE)
                {
                    sortedList.add(item)
                }

                item.parcelDTO.deliveryStatus != DeliveryStatusEnum.NOT_REGISTER.CODE
            }.toList()

            this.clear()
            this.addAll(sortedList)
            this.addAll(elseList)

            this.forEach(){
                SopoLog.d("${it.parcelDTO.alias}[${it.parcelDTO.deliveryStatus}]")

            }
        }

 */
    }
    val ongoingList: LiveData<MutableList<InquiryListItem>>
        get() = _ongoingList

    private val _completeList =
        Transformations.map(parcelRepository.getLocalCompleteParcelsLiveData()) {
            ParcelMapper.parcelListToInquiryItemList(it as MutableList<ParcelDTO>)
        }
    val completeList: LiveData<MutableList<InquiryListItem>>
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

    private val _isProgress = MutableLiveData<Boolean>()
    val isProgress: LiveData<Boolean>
        get() = _isProgress


    //TODO 앱 실행 후 처음만 실행 - 나중
    var is1st = MutableLiveData<Boolean>().apply { value = true }

    // '배송 중' 또는 '배송완료' 화면 선택의 기준
    private val _screenStatus = MutableLiveData<ScreenStatusEnum>()
    val screenStatusEnum: LiveData<ScreenStatusEnum>
        get() = _screenStatus

    // '삭제하기'에서 선택된 아이템의 개수
    var cntOfSelectedItemForDelete = MutableLiveData<Int>()

    private val _cntOfDelete = MutableLiveData<Int>()
    val cntOfDelete: LiveData<Int>
        get() = _cntOfDelete

    // '배송중' => '배송완료' 개수
    private val _cntOfBeDelivered = parcelManagementRepoImpl.getIsDeliveredCntLiveData()
    val cntOfBeDelivered: LiveData<Int>
        get() = _cntOfBeDelivered

    private val _isShowDeleteSnackBar = MutableLiveData<Boolean>()
    val isShowDeleteSnackBar: LiveData<Boolean>
        get() = _isShowDeleteSnackBar

    private val _currentTimeCount = timeCountRepoImpl.getCurrentTimeCountLiveData()
    val currentParcelCntInfo: LiveData<ParcelCntInfoEntity?>
        get() = _currentTimeCount

    // 배송완료 조회 가능한 '년월' 리스트 데이터
    private val _monthList = timeCountRepoImpl.getAllLiveData()
    val monthList: LiveData<MutableList<ParcelCntInfoEntity>>
        get() = _monthList

    private val _refreshCompleteListByOnlyLocalData = timeCountRepoImpl.getRefreshCriteriaLiveData()
    val refreshCompleteListByOnlyLocalData: LiveData<Int>
        get() = _refreshCompleteListByOnlyLocalData

    val presentOngoingParcelCnt: LiveData<Int> =
        Transformations.map(parcelRepository.getLocalOnGoingParcelCnt()) { cnt ->
            SopoLog.d("메인 조회 탭 >>> 택배 갯수: $cnt")
            cnt
        }

    val presentOngoingParcelCntAsStr: LiveData<String> =
        Transformations.map(parcelRepository.getLocalOnGoingParcelCnt()) { cnt ->
            SopoLog.d("메인 조회 탭 >>> 택배 갯수: $cnt")
            "택배 ${cnt}개"
        }

    private val ioScope = CoroutineScope(Dispatchers.IO)
    private var pagingManagement = PagingManagement(0, "", true)

    init
    {
        cntOfSelectedItemForDelete.value = 0
        _isMoreView.value = false
        _isRemovable.value = false
        _isSelectAll.value = false
        _screenStatus.value = ScreenStatusEnum.ONGOING

        sendRemovedData()
        checkIsNeedForceUpdate()
    }

    // 화면을 배송완료 ==> 배송중으로 전환시킨다.
    fun setScreenStatusOngoing()
    {
        _screenStatus.value = ScreenStatusEnum.ONGOING
    }

    // 화면을 배송중 ==> 배송완료로 전환시킨다.
    fun setScreenStatusComplete()
    {
        _screenStatus.value = ScreenStatusEnum.COMPLETE
        _isShowDeleteSnackBar.value = false
        if(is1st.value == true)
        {
            refreshComplete()
            is1st.value = false
        }
    }

    fun setCntOfDelete(value: Int)
    {
        _cntOfDelete.value = value
    }

    private fun setRemovable(flag: Boolean)
    {
        _isRemovable.value = flag
    }

    private fun setMoreView(flag: Boolean)
    {
        _isMoreView.value = flag
    }

    private fun setSelectAll(flag: Boolean)
    {
        _isSelectAll.value = flag
    }

    fun getCurrentScreenStatus(): ScreenStatusEnum?
    {
        return screenStatusEnum.value
    }

    fun refreshCompleteListByOnlyLocalData()
    {
        SopoLog.d("refreshCompleteListByOnlyLocalData() call")

        viewModelScope.launch(Dispatchers.IO) {

            //            _isProgress.postValue(true)

            //  monthList가 1개 있었을 경우 => 2개 있었을 경우 =>
            timeCountRepoImpl.getCurrentTimeCount()?.let {
                it.visibility = 0
                timeCountRepoImpl.updateEntity(it)
            }
            timeCountRepoImpl.getAll()?.let { list ->
                if(list.filter { it.count > 0 }.isNotEmpty())
                {
                    val nextVisibleEntity = list.first { it.count > 0 }
                    nextVisibleEntity.visibility = 1
                    timeCountRepoImpl.updateEntity(nextVisibleEntity)

                    getCompleteListWithPaging(nextVisibleEntity.time.replace("-", ""))
                }
                // 전부 다 존재하긴 하지만 count가 0개일때는 TimeCount 자체가 쓸모가 없는 상태로 visibility를 -1로 세팅하여
                // monthList(LiveData)에서 제외 (deleteAll로 삭제하면 '삭제취소'로 복구를 할 수가 없기 때문에 visibility를 -1로 세팅한다.
                // ( status를 0으로 수정하면 UI에서 접부 삭제했을때 monthList가 남아있어서 EmptyView가 올라오지 않는다.)
                else
                {
                    list.forEach { it.visibility = -1 }
                    timeCountRepoImpl.updateEntities(list)
                }
            }

            //            _isProgress.postValue(false)
        }
    }

    // 배송완료 리스트의 년월 리스트를 가져온다.
    private suspend fun getTimeCountList(): MutableList<TimeCountDTO>?
    {
        // 기존 TimeCount를 deleteAll하고 새로운 TimeCount를 insert 해도 되지만 , 이렇게하면 화면 UI가 CompleteList가 먼저 보이고 년월 리스트가 그 후에 보이게 되므로
        // status를 이용해서 0으로 먼저 바꾸고(비활성화) insert를 한 후 status가 0인 데이터를 전부 지운다. (화면의 부자연스러움이 사라짐)
        timeCountRepoImpl.getAll()?.let { list ->
            list.forEach { it.status = 0 }
            timeCountRepoImpl.updateEntities(list)
        }

        return parcelRepository.getRemoteMonths()?.let { list ->
            val entityList = list.map(TimeCountMapper::timeCountDtoToTimeCountEntity)
            if(entityList.isNotEmpty())
            {
                entityList.first { it.count > 0 }.visibility = 1 // visibility를 1로 세팅함으로써 화면에 노출
                timeCountRepoImpl.insertEntities(entityList)
                timeCountRepoImpl.getAll()?.let { all ->
                    all.filter { it.status == 0 }.also {
                        timeCountRepoImpl.deleteEntities(it)
                    }
                }
            }
            list
        }
    }

    // UI를 통해 사용자가 배송완료에서 조회하고 싶은 년월을 바꾼다.
    fun changeTimeCount(time: String)
    {
        viewModelScope.launch(Dispatchers.IO) {
            updateCurrentTimeCount(time) // TimeCount의 visibility를 수정한다.
            timeCountRepoImpl.getCurrentTimeCount()?.let {
                getCompleteListWithPaging(TimeCountMapper.timeCountToInquiryDate(
                    time)) // 수정된 Visibility에 해당하는 년월을 서버로 요청해서 받아온다.
            }
        }
    }

    // TimeCount의 visibility를 수정한다.
    private fun updateCurrentTimeCount(time: String)
    {
        timeCountRepoImpl.getCurrentTimeCount()?.let {
            it.visibility = 0
            timeCountRepoImpl.updateEntity(it)
        }
        timeCountRepoImpl.getById(time)?.let {
            it.visibility = 1
            timeCountRepoImpl.updateEntity(it)
        }
    }

    // 배송완료 리스트를 가져온다.(페이징 포함)
    fun getCompleteListWithPaging(inquiryDate: String)
    {
        SopoLog.d(msg = "getCompleteListWithPaging() call")
        viewModelScope.launch(Dispatchers.IO) {

            //            _isProgress.postValue(true)

            if(pagingManagement.inquiryDate != inquiryDate)
            {
                //pagingManagement에 저장된 inquiryDate랑 새로 조회하려는 데이터가 다르면 페이징 데이터 초기화 후 새로운 데이터로 조회
                pagingManagement = PagingManagement(0, inquiryDate, true)

                // TODO isNowVisible의 정체?
                // 전체 리스트(getAll) 중 isNowVisible이 1인 Entity를(filter) 0으로 바꾼 후(forEach) 업데이트(updateEntities)
                parcelManagementRepoImpl.getAll()?.let { list ->
                    list.filter { it.isNowVisible == 1 }.forEach { it.isNowVisible = 0 }
                    parcelManagementRepoImpl.updateEntities(list)
                }


            }
            else
            {
                pagingManagement.pagingNum += 1
            }
            // 다음에 조회할 데이터가 있다면
            if(pagingManagement.hasNext)
            {
                val remoteCompleteParcels = parcelRepository.getRemoteCompleteParcels(page = pagingManagement.pagingNum, inquiryDate = inquiryDate)

                // null이거나 0이면 다음 데이터가 없는 것이므로 페이징 숫자를 1빼고 hasNext를 false로 바꾼다.
                if(remoteCompleteParcels == null || remoteCompleteParcels.size == 0)
                {
                    pagingManagement.pagingNum -= 1
                    pagingManagement.hasNext = false
                    //                    _isProgress.postValue(false)
                    return@launch
                }
                else
                {
                    remoteCompleteParcels.sortByDescending { it.arrivalDte } // 도착한 시간을 기준으로 내림차순으로 정렬
                    val updateParcelList = mutableListOf<ParcelDTO>() // list에 모았다가 한번에 업데이트
                    for(parcel in remoteCompleteParcels)
                    {
                        val localParcelById =
                            parcelRepository.getLocalParcelById(parcel.parcelId)
                        updateParcelList.add(parcel)
                        if(localParcelById == null)
                        {
                            parcelManagementRepoImpl.insertEntity(
                                ParcelMapper.parcelToParcelManagementEntity(parcel)
                                    .also { it.isNowVisible = 1 })
                        }
                        else
                        {
                            parcelManagementRepoImpl.getEntity(parcel.parcelId)?.let { entity ->
                                parcelManagementRepoImpl.updateEntity(entity.also {
                                    it.isNowVisible = 1
                                })
                            }
                        }
                    }
                    parcelRepository.insertEntities(updateParcelList)

                    //                    _isProgress.postValue(false)

                }
            }
        }


    }

    //'더보기'를 눌렀다가 땠을때
    fun toggleMoreView()
    {
        _isMoreView.value?.let {
            setMoreView(!it)
        }
    }

    // 택배 삭제할때 전체선택 여부
    fun toggleSelectAll()
    {
        _isSelectAll.value?.let {
            setSelectAll(!it)
        }
    }

    // 앱이 켜졌을 때 택배의 변동사항이 있어 사용자에게 업데이트된 내용을 보여줘야할 때 강제 업데이트를 한다.
    private fun checkIsNeedForceUpdate()
    {
        viewModelScope.launch(Dispatchers.Default) {

            // 현재 가지고 있는 아이템의 수가 0개라면 새로고침 한다.
            if(parcelRepository.getOnGoingDataCnt() == 0)
            {
                refreshOngoing()
                return@launch
            }
        }
    }

    // 배송 중, 등록된 택배의 전체 새로고침
    fun refreshOngoing()
    {
        SopoLog.d("refreshOngoing() call")

        try
        {
            viewModelScope.launch(Dispatchers.IO) {

                //                _isProgress.postValue(true)

                val remoteParcels = parcelRepository.getRemoteOngoingParcels()

                // 서버로부터 데이터를 받아온 값이 널이 아니라면
                remoteParcels?.let { parcels ->

                    // 서버로부터 받아온 데이터(ParcelId로 검색)로 로컬 db에 검색이 되는지 확인한다.
                    for(remote in parcels)
                    {
                        val localParcelById =
                            parcelRepository.getLocalParcelById(remote.parcelId)
                        // 서버로부터 받아온 데이터가 검색되지 않았을 경우 => 새로운 데이터라는 의미 => 로컬에 저장한다.
                        // ParcelEntity를 관리해줄 ParcelManagementEntity도 같은 ParcelId로 저장한다.
                        if(localParcelById == null)
                        {
                            SopoLog.d(msg = """
                            remote 
                            ${remote}
                        """.trimIndent())

                            parcelRepository.insetEntity(
                                ParcelMapper.parcelToParcelEntity(remote))

                            val parcelManagement =
                                ParcelMapper.parcelToParcelManagementEntity(remote)

                            parcelManagement.run {
                                updatableStatus = 0
                                unidentifiedStatus = 0
                            }

                            SopoLog.d(msg = "업데이트 완료 => ${parcelManagement.toString()}")

                            parcelManagementRepoImpl.insertEntity(parcelManagement)
                        }
                        /*
                            서버로부터 받아온 데이터가 검색된 경우
                            => 기존에 있는 데이터라는 의미
                            => [InquiryHash를 조사해서 변화했는지 체크 및(&&) 택배의 상태가 활성화(STATUS == 1)인지 체크]
                            => 바뀐게 있다면 서버 데이터를 업데이트한다.
                         */
                        else
                        {
                            if(localParcelById.status == 1)
                            {
                                SopoLog.d(
                                    msg = "InquiryVm => PARCEL_STATUS의 'isBeUpdate'를 1 -> 0으로 초기화 작업")

                                parcelRepository.updateEntity(
                                    ParcelMapper.parcelToParcelEntity(remote))

                                val parcelManagement =
                                    ParcelMapper.parcelToParcelManagementEntity(remote)
                                // 업데이트 성공했으니 isBeUpdate를 0으로 다시 초기화시켜준다.
                                parcelManagement.run {
                                    updatableStatus = 0
                                    unidentifiedStatus = 0
                                }

                                SopoLog.d(msg = "업데이트 완료 => ${parcelManagement.toString()}")

                                parcelManagementRepoImpl.insertEntity(parcelManagement)
                            }
                        }

                    }
                }

                // 로컬에 존재하지만 새로고침하여 서버로부터 받아온 데이터 중에는 없는 데이터
                // (서버에서는 Delivered , 로컬에서는 out_for_delivery)
                // => 새로고침을 하여 GET /parcels로 받아온 데이터는 서버의 데이터베이스 기준 onGoing에 해당하는 택배들만 가져옴
                // => 따라서 로컬에서는 out_for_delivery로 배송 출발 상태(delivered 직전 상태)지만 서버에서는 delivered 상태면 새로고침 대상에서 제외됨.
                // => 이런 데이터들은 각각 GET /parcel로 요청하여 새로고침한다.
                parcelRepository.getLocalOngoingParcels()?.let { localList ->
                    remoteParcels?.let { remoteList ->
                        val filteredLocalList = localList as MutableList
                        for(remoteParcel in remoteList)
                        {
                            filteredLocalList.removeIf {
                                (it.parcelId == remoteParcel.parcelId)
                            }
                        }

                        for(parcel in filteredLocalList)
                        {
                            val result = ParcelCall.getSingleParcelTest(parcel.parcelId)

                            val remoteOngoingParcel = result.data

                            val localParcelById =
                                parcelRepository.getLocalParcelById(parcel.parcelId)
                            if(remoteOngoingParcel != null && localParcelById != null)
                            {
                                if(remoteOngoingParcel.inquiryHash != parcel.inquiryHash)
                                {
                                    parcelRepository.insetEntity(localParcelById.apply {
                                        this.update(remoteOngoingParcel)
                                    })
                                    parcelManagementRepoImpl.initializeIsBeUpdate(localParcelById.parcelId)
                                }
                            }
                        }
                    }
                }

                //                _isProgress.postValue(false)
            }
        }
        catch(e: Exception)
        {
            //            _isProgress.postValue(false)
            SopoLog.e("refreshOngoing() Error >>>", e)
        }


    }

    // 배송완료 리스트의 전체 새로고침
    fun refreshComplete()
    {
        viewModelScope.launch(Dispatchers.IO) {
            clearIsBeDelivered().join()
            sendRemovedData().join()
            initCompleteList().join()
        }
    }

    // 전체 isBeDelivered를 0으로 초기화시켜준다.
    private fun clearIsBeDelivered(): Job
    {
        return viewModelScope.launch(Dispatchers.IO) {
            parcelManagementRepoImpl.updateTotalIsBeDeliveredToZero()
        }
    }

    // 배송완료 리스트를 가져오기전 초기화 작업
    private fun initCompleteList(): Job
    {
        return viewModelScope.launch(Dispatchers.IO) {

            getTimeCountList() // 년월 리스트를 가져온다.
            timeCountRepoImpl.getCurrentTimeCount()?.let {
                getCompleteListWithPaging(MenuMapper.timeToInquiryDate(it.time))
            }
        }
    }

    // 데이터 삭제 로직 1단계 : Room의 status를 0=>1으로 바꾼다.
    fun removeSelectedData(selectedData: MutableList<Int>)
    {
        viewModelScope.launch(Dispatchers.IO) {
            parcelRepository.deleteLocalParcels(selectedData)
            parcelManagementRepoImpl.updateIsBeDeleteToOneByParcelIdList(selectedData)
            // 배송완료일때 아이템이 하나도 없을 경우 전체 새로고침을 해야해서 삭제할때 -1을 해줘야함.
            if(getCurrentScreenStatus() == ScreenStatusEnum.COMPLETE)
            {
                timeCountRepoImpl.getCurrentTimeCount()?.let {
                    it.count = it.count - selectedData.size
                    timeCountRepoImpl.updateEntity(it)
                }
            }
        }
    }

    // 데어터 삭제 로직 2단계 : Room의 status 3을 가진 택배들을 전부 서버로 보내서 서버 데이터 역시 삭제(1==>0)하고 최종적으로 status를 0으로 바꿔서 삭제 로직을 마무리한다.
    private fun sendRemovedData(): Job
    {
        return ioScope.launch {
            try
            {
                // 서버로 데이터를 삭제(상태 업데이트)하라고 요청
                val deleteRemoteParcels = parcelRepository.deleteRemoteParcels()
                // 위 요청이 성공했다면 (삭제할 데이터가 없으면 null임)
                if(deleteRemoteParcels?.code == ResponseCode.SUCCESS.CODE)
                {
                    // 해당 아이템의 status(PARCEL)를 0으로 업데이트하여 삭제 처리를 마무리
                    val isBeDeleteList = parcelManagementRepoImpl.getAll()?.let { parcelMng ->
                        parcelMng.filter { it.isBeDelete == 1 }
                    }
                    isBeDeleteList?.let { list ->
                        list.forEach {
                            it.isBeDelete = 0
                            it.auditDte = TimeUtil.getDateTime()
                        }
                        parcelManagementRepoImpl.updateEntities(list)
                    }
                    SopoLog.i("SUCCESS to send delete data")
                }
            }
            catch(e: HttpException)
            {
                //TODO : 삭제하기에서 실패했을때 예외처리.
                val errorLog = e.response()?.errorBody()?.charStream()
                val apiResult = Gson().fromJson(errorLog, APIResult::class.java)
                SopoLog.i("Fail to send delete data : ${apiResult.message}") // 실패 로그
            }
        }
    }

    // 삭제취소를 눌렀을 때
    fun deleteCancel()
    {
        viewModelScope.launch(Dispatchers.IO) {
            _isShowDeleteSnackBar.postValue(false) // 삭제 스낵바 바로 dismiss
            val cancelDataList = parcelManagementRepoImpl.getCancelIsBeDelete()
            SopoLog.d(msg = "삭제 취소할 데이터 : $cancelDataList")

            cancelDataList?.let { parcelMngList ->
                // PARCEL_STATUS의 isBeDelete를 0으로 다시 초기화
                parcelMngList.forEach { it.isBeDelete = 0 }
                parcelManagementRepoImpl.updateEntities(parcelMngList)

                for(parcelMng in parcelMngList)
                {
                    parcelRepository.getLocalParcelById(
                        parcelMng.parcelId)?.let {
                        it.status = 1
                        parcelRepository.updateEntity(it)
                    }
                }
                if(getCurrentScreenStatus() == ScreenStatusEnum.COMPLETE)
                {
                    // 복구해야할 리스트 중 아이템 하나의 도착일자 (2020-09-19 ~~)에서 PARCEL_CNT_INFO의 primaryKey를 추출해서 복구해야할 PARCEL_CNT_INFO를 구한다.
                    parcelRepository.getLocalParcelById(
                        cancelDataList.first().parcelId)
                        ?.let {
                            val timeCountPrimaryKey =
                                TimeCountMapper.arrivalDateToTime(it.arrivalDte)
                            timeCountRepoImpl.getLatestUpdatedEntity(timeCountPrimaryKey)
                                ?.let { entity ->
                                    entity.count += cancelDataList.size
                                    entity.visibility =
                                        0 // 모든 아이템(monthList)가 삭제되었을때 삭제취소를 하려면 visibility를 0으로 수정해줘야한다.

                                    SopoLog.d(
                                        "복구해야할 PARCEL_CNT_INFO => time : ${entity.time} , count : ${entity.count}, status : ${entity.status} , auditDate : ${entity.auditDte}")
                                    timeCountRepoImpl.updateEntity(entity)
                                }
                        }
                }
            }
        }
    }

    // '삭제하기'에서 아이템이 전부 선택되었는지를 반환
    fun isFullySelected(selectedNum: Int): Boolean
    {
        return when(screenStatusEnum.value ?: ScreenStatusEnum.ONGOING)
        {
            ScreenStatusEnum.ONGOING ->
            {
                selectedNum == (_ongoingList.value?.size ?: 0)
            }
            ScreenStatusEnum.COMPLETE ->
            {
                selectedNum == (completeList.value?.size ?: 0)
            }
        }
    }

    // 삭제하기 화면 open
    fun openRemoveView()
    {
        setRemovable(true) // 리스트들의 아이템들을 삭제할 수 있게 지시한다.
        setMoreView(
            true) // '삭제하기'를 선택했을때 '더 보기'로 숨겨져있던 아이템들도 모두 선택할 수 있어야하므로 해당 liveData를 true로 바꿔줘서 화면의 변화를 지시한다.
    }

    // 삭제하기 화면 close
    fun closeRemoveView()
    {
        cntOfSelectedItemForDelete.value = 0
        setRemovable(false)
        setMoreView(false)
    }

    fun patchParcelAlias(updateAliasRequest: UpdateAliasRequest)
    {
        CoroutineScope(Dispatchers.Main).launch {
            ParcelUseCase.updateParcelAlias(req = updateAliasRequest)
        }
    }

    private fun sortByDeliveryStatus(list: List<InquiryListItem>): List<InquiryListItem>{

        val sortedList = mutableListOf<InquiryListItem>()
        val multiList = listOf<MutableList<InquiryListItem>>(
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf(),
            mutableListOf()
        )

        val elseList = list.asSequence().filter { item ->

            if(item.parcelDTO.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE)
            {
                SopoLog.d("배송완료(Delivered)[${item.parcelDTO.alias}]")
                multiList[0].add(item)
            }

            item.parcelDTO.deliveryStatus != DeliveryStatusEnum.DELIVERED.CODE
        }.filter { item ->
            if(item.parcelDTO.deliveryStatus == DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE)
            {
                SopoLog.d("동네도착(out_for_delivery)[${item.parcelDTO.alias}]")
                multiList[1].add(item)
            }

            item.parcelDTO.deliveryStatus != DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE
        }.filter { item ->
            if(item.parcelDTO.deliveryStatus == DeliveryStatusEnum.IN_TRANSIT.CODE)
            {
                SopoLog.d("배송중(in_transit)[${item.parcelDTO.alias}]")
                multiList[2].add(item)
            }

            item.parcelDTO.deliveryStatus != DeliveryStatusEnum.IN_TRANSIT.CODE
        }.filter { item ->
            if(item.parcelDTO.deliveryStatus == DeliveryStatusEnum.AT_PICKUP.CODE)
            {
                SopoLog.d("픽업(at_pickup)[${item.parcelDTO.alias}]")
                multiList[3].add(item)
            }

            item.parcelDTO.deliveryStatus != DeliveryStatusEnum.AT_PICKUP.CODE
        }.filter { item ->
            if(item.parcelDTO.deliveryStatus == DeliveryStatusEnum.INFORMATION_RECEIVED.CODE)
            {
                SopoLog.d("접수(information_received)[${item.parcelDTO.alias}]")
                multiList[4].add(item)
            }

            item.parcelDTO.deliveryStatus != DeliveryStatusEnum.INFORMATION_RECEIVED.CODE
        }.filter { item ->
            if(item.parcelDTO.deliveryStatus == DeliveryStatusEnum.NOT_REGISTER.CODE)
            {
                SopoLog.d("미등록(not_register)[${item.parcelDTO.alias}]")
                multiList[5].add(item)
            }

            item.parcelDTO.deliveryStatus != DeliveryStatusEnum.NOT_REGISTER.CODE
        }.toList()

        multiList[6].addAll(elseList)

        multiList.forEach {
            Collections.sort(it, SortByDate())
            sortedList.addAll(it)
        }

        sortedList.forEach(){
            SopoLog.d("${it.parcelDTO.alias}[${it.parcelDTO.deliveryStatus}][${it.parcelDTO.auditDte}]")
        }

        return sortedList
    }

    class SortByDate: Comparator<InquiryListItem>{
        override fun compare(p0: InquiryListItem, p1: InquiryListItem): Int
        {
            return p0.parcelDTO.auditDte.compareTo(p1.parcelDTO.auditDte)
        }
    }
}