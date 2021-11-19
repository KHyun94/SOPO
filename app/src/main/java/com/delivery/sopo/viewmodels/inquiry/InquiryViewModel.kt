package com.delivery.sopo.viewmodels.inquiry

import android.widget.TextView
import androidx.lifecycle.*
import com.delivery.sopo.UserExceptionHandler
import com.delivery.sopo.data.repository.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.data.repository.local.repository.CompletedParcelHistoryRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.data.repository.remote.parcel.ParcelUseCase
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.InquiryStatusEnum
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.UpdateAliasRequest
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.inquiry.PagingManagement
import com.delivery.sopo.models.mapper.CompletedParcelHistoryMapper
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.ParcelResponse
import com.delivery.sopo.usecase.parcel.GetCompleteParcelUseCase
import com.delivery.sopo.usecase.parcel.RefreshParcelsUseCase
import com.delivery.sopo.usecase.parcel.SyncParcelsUseCase
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*
import java.util.*

class InquiryViewModel(
        private val getCompleteParcelUseCase: GetCompleteParcelUseCase,
        private val refreshParcelsUseCase: RefreshParcelsUseCase,
        private val syncParcelsUseCase: SyncParcelsUseCase,
        private val parcelRepo: ParcelRepository,
        private val parcelManagementRepo: ParcelManagementRepoImpl,
        private val historyRepo: CompletedParcelHistoryRepoImpl):
        BaseViewModel()
{
    /**
     * 공용
     */

    // '배송 중' 또는 '배송완료' 화면 선택의 기준
    private val _inquiryStatus = MutableLiveData<InquiryStatusEnum>().apply { value = InquiryStatusEnum.ONGOING }
    val inquiryStatus: LiveData<InquiryStatusEnum>
        get() = _inquiryStatus

    // '배송중' => '배송완료' 개수
    private val _cntOfBeDelivered = parcelManagementRepo.getIsDeliveredCntLiveData()
    val cntOfBeDelivered: LiveData<Int>
        get() = _cntOfBeDelivered

    private var isFirstLoading: Boolean = false

    /**
     * 현재 진행 중인 택배 페이지
     */

    private var _ongoingList = Transformations.map(parcelRepo.getLocalOngoingParcelsAsLiveData()) { parcelList ->
            val list: MutableList<InquiryListItem> = ParcelMapper.parcelListToInquiryItemList(parcelList)
            sortByDeliveryStatus(list).toMutableList()
        }
    val ongoingList: LiveData<MutableList<InquiryListItem>>
        get() = _ongoingList

    val cntOfPresentOngoingParcels: LiveData<Int> =
        Transformations.map(parcelRepo.getLocalOnGoingParcelCnt()) { cnt ->
            cnt
        }

    // 화면에 전체 아이템의 노출 여부
    private val _isMoreView = MutableLiveData<Boolean>().apply { value =  false }
    val isMoreView: LiveData<Boolean>
        get() = _isMoreView

    /**
     * 완료된 택배 페이지
     */

    private var _completeList = MutableLiveData<MutableList<InquiryListItem>>()
    val completeList: LiveData<MutableList<InquiryListItem>>
        get() = _completeList

    // 배송완료 조회 가능한 'Calendar'
    val histories: LiveData<List<CompletedParcelHistory>>
        get() = historyRepo.getAllAsLiveData()

    var isClickableMonths: Boolean = true

    val yearOfCalendar = MutableLiveData<String>()
    val monthsOfCalendar = MutableLiveData<List<SelectItem<CompletedParcelHistory>>>()
    val selectedDate = MutableLiveData<String>()

    private var pagingManagement = PagingManagement(0, "", true)

    init
    {

        viewModelScope.launch(Dispatchers.Main) {
            syncParcelsByOngoing()
            requestCompletedParcelHistory()
            pagingManagement = PagingManagement(0, "", true)
        }
    }

    /**
     * 이벤트 리스너
     */

    // 조회 - 배송 상태를 '배송 중'으로 변경
    fun onChangedOngoingClicked()
    {
        _inquiryStatus.value = InquiryStatusEnum.ONGOING
    }

    // 화면을 배송중 ==> 배송완료로 전환시킨다.
    fun onChangedCompleteClicked()
    {
        _inquiryStatus.value = InquiryStatusEnum.COMPLETE

        if(isFirstLoading) return
        isFirstLoading = !isFirstLoading

        refreshCompleteParcels()
    }

    fun getCurrentScreenStatus(): InquiryStatusEnum?
    {
        return inquiryStatus.value
    }

    fun changeCompletedParcelHistoryDate(year: String)
    {
        pagingManagement = PagingManagement(0, "", true)
        yearOfCalendar.postValue(year)
        updateMonthsSelector(year)
    }

    fun refreshCompleteListByOnlyLocalData()
    {
        SopoLog.d("refreshCompleteListByOnlyLocalData() call")

        viewModelScope.launch(Dispatchers.IO) {

            //            _isProgress.postValue(true)

            //  monthList가 1개 있었을 경우 => 2개 있었을 경우 =>
            historyRepo.getCurrentTimeCount()?.let {
                it.visibility = 0
                historyRepo.updateEntity(it)
            }
            historyRepo.getAll()?.let { list ->
                if(list.filter { it.count > 0 }.isNotEmpty())
                {
                    val nextVisibleEntity = list.first { it.count > 0 }
                    nextVisibleEntity.visibility = 1
                    historyRepo.updateEntity(nextVisibleEntity)

                    getCompleteParcelsWithPaging(nextVisibleEntity.date.replace("-", ""))
                }
                // 전부 다 존재하긴 하지만 count가 0개일때는 TimeCount 자체가 쓸모가 없는 상태로 visibility를 -1로 세팅하여
                // monthList(LiveData)에서 제외 (deleteAll로 삭제하면 '삭제취소'로 복구를 할 수가 없기 때문에 visibility를 -1로 세팅한다.
                // ( status를 0으로 수정하면 UI에서 접부 삭제했을때 monthList가 남아있어서 EmptyView가 올라오지 않는다.)
                else
                {
                    list.forEach { it.visibility = -1 }
                    historyRepo.updateEntities(list)
                }
            }
        }
    }

    // 배송완료 리스트의 년월 리스트를 가져온다.
    private suspend fun requestCompletedParcelHistory(): List<CompletedParcelHistory>
    {
        SopoLog.i("requestCompletedParcelHistory(...) 호출")

        val histories = withContext(Dispatchers.IO) { parcelRepo.getRemoteMonths() }

        SopoLog.d("Completed Parcel Date 리스트 사이즈 - ${histories.size}")

        withContext(Dispatchers.Default) {
            val entities = histories.map(CompletedParcelHistoryMapper::dtoToEntity)
            historyRepo.insertEntities(entities)
        }

        SopoLog.d("Completed Parcel Date insert")

        return histories
    }

    fun updateCompletedParcelCalendar(year: String)
    {
        updateYearSpinner(year = year)
        updateMonthsSelector(year = year)
        _completeList.postValue(emptyList<InquiryListItem>().toMutableList())
    }

    fun updateYearSpinner(year: String)
    {
        SopoLog.i("updateYearSpinner(...) 호출 [data:$year]")
        yearOfCalendar.postValue(year)
    }

    // UI를 통해 사용자가 배송완료에서 조회하고 싶은 년월을 바꾼다.
    fun updateMonthsSelector(year: String) = viewModelScope.launch(Dispatchers.Default) {

        SopoLog.i("updateMonthsSelector(...) 호출 [data:$year]")

        var isLastMonth = false

        val histories = withContext(Dispatchers.Default) {
            historyRepo.findById("${year}%").map {

                val isSelected = if(it.count > 0 && !isLastMonth)
                {
                    isLastMonth = true
                    true
                }
                else
                {
                    false
                }

                SelectItem<CompletedParcelHistory>(item = it, isSelect = isSelected)
            }
        }
        monthsOfCalendar.postValue(histories)
    }

    fun onMonthClicked(tv: TextView, month: Int)
    {
        if(!isClickableMonths) return SopoLog.d("$month 비활성화")

        SopoLog.d("$month 활성화")

        _completeList.postValue(emptyList<InquiryListItem>().toMutableList())

        val list = monthsOfCalendar.value?.map {
            val selectMonth = month.toString().padStart(2, '0')
            it.isSelect = (selectMonth == it.item.month)
            it
        } ?: return

        monthsOfCalendar.postValue(list)
    }

    //'더보기'를 눌렀다가 땠을때
    fun onToggleChangedClicked()
    {
        val beforeStatus = _isMoreView.value ?: false
        _isMoreView.postValue(!beforeStatus)
    }

    fun onRefreshParcelsClicked(){
        SopoLog.i("onRefreshParcelsClicked(...) 호출")

        try
        {
            refreshParcelsUseCase.invoke()
        }
        catch(e: Exception)
        {

        }
    }

    fun syncParcelsByOngoing() = scope.launch(Dispatchers.IO) {
        syncParcelsUseCase.invoke().start()
    }


    // 배송완료 리스트의 전체 새로고침
    fun refreshCompleteParcels()
    {
        viewModelScope.launch(Dispatchers.IO) {
            clearDeliveredStatus().join()
            //            sendRemovedData().join()
            //            initCompleteList().join()
        }
    }

    fun refreshCompleteParcelsByDate(inquiryDate: String) = CoroutineScope(Dispatchers.IO).launch {
        val list = getCompleteParcelsWithPaging(inquiryDate = inquiryDate).map {
            InquiryListItem(it, false)
        }.toMutableList()

        val previousParcels = _completeList.value?: emptyList<InquiryListItem>()
        val currentParcels = previousParcels + list

        _completeList.postValue(currentParcels.toMutableList())
    }


    // 배송완료 리스트를 가져온다.(페이징 포함)
    suspend fun getCompleteParcelsWithPaging(inquiryDate: String): List<ParcelResponse>
    {
        SopoLog.i("getCompleteListWithPaging(...) 호출 [data:$inquiryDate]")

        if(pagingManagement.inquiryDate != inquiryDate)
        {
            pagingManagement = PagingManagement(0, inquiryDate, true)
        }

        if(!pagingManagement.hasNext) return emptyList()

        val completeParcels = getCompleteParcelUseCase.invoke(pagingManagement)

        pagingManagement = if(completeParcels.isEmpty())
        {
            with(pagingManagement) { PagingManagement(pagingNum - 1, this.inquiryDate, false) }
        }
        else
        {
            with(pagingManagement) { PagingManagement(pagingNum + 1, this.inquiryDate, true) }
        }

        return completeParcels
    }


    // 전체 배송 완료로 표시된 상태를 1-> 0 으로 초기화시켜준다.
    private fun clearDeliveredStatus(): Job
    {
        return viewModelScope.launch(Dispatchers.IO) {
            parcelManagementRepo.updateTotalIsBeDeliveredToZero()
        }
    }


    fun onUpdateParcelAlias(updateAliasRequest: UpdateAliasRequest)
    {
        CoroutineScope(Dispatchers.Main).launch {
            ParcelUseCase.updateParcelAlias(req = updateAliasRequest)
        }
    }

    private fun sortByDeliveryStatus(list: List<InquiryListItem>): List<InquiryListItem>
    {

        val sortedList = mutableListOf<InquiryListItem>()
        val multiList =
            listOf<MutableList<InquiryListItem>>(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())

        val elseList = list.asSequence().filter { item ->

            if(item.parcelResponse.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE)
            {
                multiList[0].add(item)
            }

            item.parcelResponse.deliveryStatus != DeliveryStatusEnum.DELIVERED.CODE
        }.filter { item ->
            if(item.parcelResponse.deliveryStatus == DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE)
            {
                multiList[1].add(item)
            }

            item.parcelResponse.deliveryStatus != DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE
        }.filter { item ->
            if(item.parcelResponse.deliveryStatus == DeliveryStatusEnum.IN_TRANSIT.CODE)
            {
                multiList[2].add(item)
            }

            item.parcelResponse.deliveryStatus != DeliveryStatusEnum.IN_TRANSIT.CODE
        }.filter { item ->
            if(item.parcelResponse.deliveryStatus == DeliveryStatusEnum.AT_PICKUP.CODE)
            {
                multiList[3].add(item)
            }

            item.parcelResponse.deliveryStatus != DeliveryStatusEnum.AT_PICKUP.CODE
        }.filter { item ->
            if(item.parcelResponse.deliveryStatus == DeliveryStatusEnum.INFORMATION_RECEIVED.CODE)
            {
                multiList[4].add(item)
            }

            item.parcelResponse.deliveryStatus != DeliveryStatusEnum.INFORMATION_RECEIVED.CODE
        }.filter { item ->
            if(item.parcelResponse.deliveryStatus == DeliveryStatusEnum.NOT_REGISTERED.CODE)
            {
                //                SopoLog.d("미등록(not_register)[${item.parcelDTO.alias}]")
                multiList[5].add(item)
            }

            item.parcelResponse.deliveryStatus != DeliveryStatusEnum.NOT_REGISTERED.CODE
        }.toList()

        multiList[6].addAll(elseList)

        multiList.forEach {
            Collections.sort(it, SortByDate())
            sortedList.addAll(it)
        }
        return sortedList
    }

    class SortByDate: Comparator<InquiryListItem>
    {
        override fun compare(p0: InquiryListItem, p1: InquiryListItem): Int
        {
            return p0.parcelResponse.auditDte.compareTo(p1.parcelResponse.auditDte)
        }
    }

    private val onSOPOErrorCallback = object: OnSOPOErrorCallback
    {
        override fun onFailure(error: ErrorEnum)
        {
        }
    }
    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        UserExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }
}