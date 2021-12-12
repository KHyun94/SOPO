package com.delivery.sopo.viewmodels.inquiry

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.lifecycle.*
import com.delivery.sopo.UserExceptionHandler
import com.delivery.sopo.data.repository.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.data.repository.local.repository.CompletedParcelHistoryRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.InquiryStatusEnum
import com.delivery.sopo.extensions.MutableLiveDataExtension.initialize
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.UpdateParcelAliasRequest
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.inquiry.PagingManagement
import com.delivery.sopo.models.mapper.CompletedParcelHistoryMapper
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.ParcelResponse
import com.delivery.sopo.usecase.parcel.remote.*
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*
import java.util.*

class DeleteParcelViewModel(
        private val getCompleteParcelUseCase: GetCompleteParcelUseCase,
        private val refreshParcelsUseCase: RefreshParcelsUseCase,
        private val refreshParcelUseCase: RefreshParcelUseCase,
        private val syncParcelsUseCase: SyncParcelsUseCase,
        private val getCompletedMonthUseCase: GetCompletedMonthUseCase,
        private val updateParcelAliasUseCase: UpdateParcelAliasUseCase,
        private val parcelRepo: ParcelRepository,
        private val parcelManagementRepo: ParcelManagementRepoImpl,
        private val historyRepo: CompletedParcelHistoryRepoImpl):
        BaseViewModel()
{
    /**
     * 공용
     */ // '배송 중' 또는 '배송완료' 화면 선택의 기준
    private val _inquiryStatus =
        MutableLiveData<InquiryStatusEnum>().initialize(InquiryStatusEnum.ONGOING)
    val inquiryStatus: LiveData<InquiryStatusEnum>
        get() = _inquiryStatus

    // '배송중' => '배송완료' 개수
    private val _cntOfBeDelivered = parcelManagementRepo.getIsDeliveredCntLiveData()
    val cntOfBeDelivered: LiveData<Int>
        get() = _cntOfBeDelivered


    private val _isAvailableRefresh = MutableLiveData<Boolean>().initialize(true)
    val isAvailableRefresh: LiveData<Boolean>
        get() = _isAvailableRefresh

    private var isFirstLoading: Boolean = false

    // '삭제하기'에서 선택된 아이템의 개수
    var cntOfSelectedItemForDelete = MutableLiveData<Int>()

    /**
     * 현재 진행 중인 택배 페이지
     */

    private var _ongoingList =
        Transformations.map(parcelRepo.getLocalOngoingParcelsAsLiveData()) { parcelList ->
            val list: MutableList<InquiryListItem> =
                ParcelMapper.parcelListToInquiryItemList(parcelList)
            sortByDeliveryStatus(list).toMutableList()
        }
    val ongoingList: LiveData<MutableList<InquiryListItem>>
        get() = _ongoingList

    val cntOfPresentOngoingParcels: LiveData<Int> =
        Transformations.map(parcelRepo.getLocalOnGoingParcelCnt()) { cnt ->
            cnt
        }

    /**
     * 완료된 택배 페이지
     */

    private var _completeList = MutableLiveData<MutableList<InquiryListItem>>()
    val completeList: LiveData<MutableList<InquiryListItem>>
        get() = _completeList

    // 배송완료 조회 가능한 'Calendar'
    val histories: LiveData<List<CompletedParcelHistory>>
        get() = historyRepo.getAllAsLiveData()

    var isMonthClickable: Boolean = true

    val yearOfCalendar = MutableLiveData<String>().apply { postValue("2021") }
    val monthsOfCalendar = MutableLiveData<List<SelectItem<CompletedParcelHistory>>>()
    val selectedDate = MutableLiveData<String>()

    private var pagingManagement = PagingManagement(0, "", true)

    init
    {
        viewModelScope.launch(Dispatchers.Main) {
            syncParcelsByOngoing()
            getCompletedMonthUseCase.invoke()
            pagingManagement = PagingManagement(0, "", true)
        }
    }

    /**
     * 이벤트 리스너
     */

    fun setInquiryStatus(statusEnum: InquiryStatusEnum)
    {
        _inquiryStatus.value = statusEnum
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

    fun getRemoteCompletedMonth() = scope.launch {
        try
        {
            getCompletedMonthUseCase.invoke()
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }
    }

    fun refreshCompleteListByOnlyLocalData() = scope.launch(Dispatchers.IO) {
        SopoLog.d("refreshCompleteListByOnlyLocalData() call")

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
            } // 전부 다 존재하긴 하지만 count가 0개일때는 TimeCount 자체가 쓸모가 없는 상태로 visibility를 -1로 세팅하여
            // monthList(LiveData)에서 제외 (deleteAll로 삭제하면 '삭제취소'로 복구를 할 수가 없기 때문에 visibility를 -1로 세팅한다.
            // ( status를 0으로 수정하면 UI에서 접부 삭제했을때 monthList가 남아있어서 EmptyView가 올라오지 않는다.)
            else
            {
                list.forEach { it.visibility = -1 }
                historyRepo.updateEntities(list)
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
            historyRepo.deleteAll()
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
        if(!isMonthClickable) return SopoLog.d("$month 비활성화")

        SopoLog.d("$month 활성화")

        _completeList.postValue(emptyList<InquiryListItem>().toMutableList())

        val list = monthsOfCalendar.value?.map {
            val selectMonth = month.toString().padStart(2, '0')
            it.isSelect = (selectMonth == it.item.month)
            it
        } ?: return

        monthsOfCalendar.postValue(list)
    }

    fun onMoveToRegister()
    {

    }

    fun onRefreshParcelsClicked() = scope.launch {
        SopoLog.i("onRefreshParcelsClicked(...) 호출")

        _isAvailableRefresh.postValue(false)

        try
        {
            refreshParcelsUseCase.invoke()
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }
        finally
        {
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                _isAvailableRefresh.postValue(true)
            }, 3000)

        }
    }

    fun syncParcelsByOngoing() = scope.launch(Dispatchers.IO) {
        try
        {
            syncParcelsUseCase.invoke()
        }
        catch(e: Exception)
        {
            exceptionHandler.handleException(coroutineContext, e)
        }
    }

    // 배송완료 리스트의 전체 새로고침
    fun refreshCompleteParcels() = scope.launch(Dispatchers.IO) {
        clearDeliveredStatus().join()
    }

    fun refreshCompleteParcelsByDate(inquiryDate: String) = CoroutineScope(Dispatchers.IO).launch {
        val list = getCompleteParcelsWithPaging(inquiryDate = inquiryDate).map {
            InquiryListItem(it, false)
        }.toMutableList()

        _completeList.postValue(list)
    }


    // 배송완료 리스트를 가져온다.(페이징 포함)
    suspend fun getCompleteParcelsWithPaging(inquiryDate: String): List<ParcelResponse>
    {
        SopoLog.i("getCompleteListWithPaging(...) 호출 [date:$inquiryDate]")

        if(pagingManagement.inquiryDate != inquiryDate)
        {
            pagingManagement = PagingManagement(0, inquiryDate, true)
        }

        if(!pagingManagement.hasNext) return emptyList()

        val completeParcels = getCompleteParcelUseCase.invoke(pagingManagement)

        pagingManagement = if(completeParcels.size < 10)
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


    fun onUpdateParcelAlias(parcelId: Int, parcelAlias: String) =
        checkEventStatus(checkNetwork = true) {
            scope.launch {
                try
                {
                    updateParcelAliasUseCase.invoke(parcelId = parcelId, parcelAlias = parcelAlias)
                }
                catch(e: Exception)
                {
                    exceptionHandler.handleException(coroutineContext, e)
                }
            }
        }

    suspend fun onRefreshParcel(parcelId: Int)  = withContext(Dispatchers.IO){
            try
            {
                refreshParcelUseCase.invoke(parcelId = parcelId)
            }
            catch(e: Exception)
            {
                exceptionHandler.handleException(coroutineContext, e)
            }
        }


    fun onDeleteParcel(parcelId: Int) = checkEventStatus(checkNetwork = true) {
        CoroutineScope(Dispatchers.IO).launch {
            try
            {
                refreshParcelUseCase.invoke(parcelId = parcelId)
            }
            catch(e: Exception)
            {
                exceptionHandler.handleException(coroutineContext, e)

            }
        }
    }

    private fun sortByDeliveryStatus(list: List<InquiryListItem>): List<InquiryListItem>
    {

        val sortedList = mutableListOf<InquiryListItem>()
        val multiList =
            listOf<MutableList<InquiryListItem>>(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())

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
            { //                SopoLog.d("미등록(not_register)[${item.parcelDTO.alias}]")
                multiList[5].add(item)
            }

            item.parcelResponse.deliveryStatus != DeliveryStatusEnum.NOT_REGISTERED.CODE
        }.filter { item ->
            if(item.parcelResponse.deliveryStatus == DeliveryStatusEnum.ORPHANED.CODE)
            { //                SopoLog.d("미등록(not_register)[${item.parcelDTO.alias}]")
                multiList[6].add(item)
            }

            item.parcelResponse.deliveryStatus != DeliveryStatusEnum.ORPHANED.CODE
        }.toList()

        multiList[7].addAll(elseList)

        multiList.forEach {
            Collections.sort(it, SortByDate())
            sortedList.addAll(it)
        }
        return sortedList
    }

    fun onSelectAllItemClicked(){

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
        override fun onRegisterParcelError(error: ErrorEnum)
        {
            super.onRegisterParcelError(error)

            postErrorSnackBar(error.message)
        }

        override fun onFailure(error: ErrorEnum)
        {
            postErrorSnackBar("알 수 없는 이유로 등록에 실패했습니다.[${error.toString()}]")
        }

        override fun onInternalServerError(error: ErrorEnum)
        {
            super.onInternalServerError(error)

            postErrorSnackBar("일시적으로 서비스를 이용할 수 없습니다.[${error.toString()}]")
        }

        override fun onAuthError(error: ErrorEnum)
        {
            super.onAuthError(error)

            postErrorSnackBar("유저 인증에 실패했습니다. 다시 시도해주세요.[${error.toString()}]")
        }
    }
    override val exceptionHandler: CoroutineExceptionHandler by lazy {
        UserExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }
}