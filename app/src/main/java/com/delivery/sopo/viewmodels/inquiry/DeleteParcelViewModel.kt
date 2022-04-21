package com.delivery.sopo.viewmodels.inquiry

import android.widget.TextView
import androidx.appcompat.widget.AppCompatCheckBox
import androidx.lifecycle.*
import com.delivery.sopo.data.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.data.repository.local.repository.CompletedParcelHistoryRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.enums.InquiryStatusEnum
import com.delivery.sopo.enums.NavigatorEnum
import com.delivery.sopo.extensions.MutableLiveDataExtension.initialize
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.inquiry.PagingManagement
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.usecase.parcel.remote.*
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*
import java.util.*

class DeleteParcelViewModel(private val getCompleteParcelUseCase: GetCompleteParcelUseCase, private val getCompletedMonthUseCase: GetCompletedMonthUseCase, private val parcelRepo: ParcelRepository, private val parcelManagementRepo: ParcelManagementRepoImpl, private val historyRepo: CompletedParcelHistoryRepoImpl):
        BaseViewModel()
{
    /**
     * 공용
     */ // '배송 중' 또는 '배송완료' 화면 선택의 기준
    private val _inquiryStatus =
        MutableLiveData<InquiryStatusEnum>().initialize(InquiryStatusEnum.ONGOING)
    val inquiryStatus: LiveData<InquiryStatusEnum>
        get() = _inquiryStatus

    // '삭제하기'에서 선택된 아이템의 개수
    var cntOfSelectedItemForDelete = MutableLiveData<Int>()

    private val _navigator = MutableLiveData<NavigatorEnum>()
    val navigator: LiveData<NavigatorEnum>
        get() = _navigator

    private val _isSelectAllItems = MutableLiveData<Boolean>()
    val isSelectAllItems: LiveData<Boolean>
        get() = _isSelectAllItems

    /**
     * 현재 진행 중인 택배 페이지
     */

    private var _ongoingList =
        Transformations.map(parcelRepo.getOngoingParcelAsLiveData()) { parcelList ->
            val list = parcelList.map { parcel ->
                InquiryListItem(parcel, false)
            }

            sortByDeliveryStatus(list).toMutableList()
        }
    val ongoingList: LiveData<MutableList<InquiryListItem>>
        get() = _ongoingList

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
        pagingManagement = PagingManagement(0, "", true)
    }

    /**
     * 이벤트 리스너
     */

    fun setIsSelectAllItems(value: Boolean)
    {
        _isSelectAllItems.postValue(value)
    }

    fun setInquiryStatus(statusEnum: InquiryStatusEnum)
    {
        _inquiryStatus.value = statusEnum
    }

    fun getCurrentScreenStatus(): InquiryStatusEnum?
    {
        return inquiryStatus.value
    }

    fun getCompleteParcelMonth() = scope.launch(coroutineExceptionHandler) {
        getCompletedMonthUseCase.invoke()
    }

    fun changeCompletedParcelHistoryDate(year: String)
    {
        pagingManagement = PagingManagement(0, "", true)
        yearOfCalendar.postValue(year)
        updateMonthsSelector(year)
    }

    fun updateCompletedParcelCalendar(year: String)
    {
        updateYearSpinner(year = year)
        updateMonthsSelector(year = year)
        _completeList.postValue(emptyList<InquiryListItem>().toMutableList())
    }

    private fun updateYearSpinner(year: String)
    {
        SopoLog.i("updateYearSpinner(...) 호출 [data:$year]")
        yearOfCalendar.postValue(year)
    }

    // UI를 통해 사용자가 배송완료에서 조회하고 싶은 년월을 바꾼다.
    private fun updateMonthsSelector(year: String) = scope.launch(Dispatchers.Default) {

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

    fun refreshCompleteParcelsByDate(inquiryDate: String) = scope.launch(coroutineExceptionHandler) {
        val list = getCompleteParcelsWithPaging(inquiryDate = inquiryDate).map { parcel ->
            InquiryListItem(parcel, false)
        }.toMutableList()

        _completeList.postValue(list)
    }


    // 배송완료 리스트를 가져온다.(페이징 포함)
    suspend fun getCompleteParcelsWithPaging(inquiryDate: String): List<Parcel.Common>
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

    fun onClearClicked()
    {
        _navigator.value = NavigatorEnum.INQUIRY_PARCEL
    }

    private fun sortByDeliveryStatus(list: List<InquiryListItem>): List<InquiryListItem>
    {
        val sortedList = mutableListOf<InquiryListItem>()
        val multiList =
            listOf<MutableList<InquiryListItem>>(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())

        val elseList = list.asSequence().filter { item ->

            if(item.parcel.deliveryStatus == DeliveryStatusEnum.DELIVERED.CODE)
            {
                multiList[0].add(item)
            }

            item.parcel.deliveryStatus != DeliveryStatusEnum.DELIVERED.CODE
        }.filter { item ->
            if(item.parcel.deliveryStatus == DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE)
            {
                multiList[1].add(item)
            }

            item.parcel.deliveryStatus != DeliveryStatusEnum.OUT_FOR_DELIVERY.CODE
        }.filter { item ->
            if(item.parcel.deliveryStatus == DeliveryStatusEnum.IN_TRANSIT.CODE)
            {
                multiList[2].add(item)
            }

            item.parcel.deliveryStatus != DeliveryStatusEnum.IN_TRANSIT.CODE
        }.filter { item ->
            if(item.parcel.deliveryStatus == DeliveryStatusEnum.AT_PICKUP.CODE)
            {
                multiList[3].add(item)
            }

            item.parcel.deliveryStatus != DeliveryStatusEnum.AT_PICKUP.CODE
        }.filter { item ->
            if(item.parcel.deliveryStatus == DeliveryStatusEnum.INFORMATION_RECEIVED.CODE)
            {
                multiList[4].add(item)
            }

            item.parcel.deliveryStatus != DeliveryStatusEnum.INFORMATION_RECEIVED.CODE
        }.filter { item ->
            if(item.parcel.deliveryStatus == DeliveryStatusEnum.NOT_REGISTERED.CODE)
            { //                SopoLog.d("미등록(not_register)[${item.parcelDTO.alias}]")
                multiList[5].add(item)
            }

            item.parcel.deliveryStatus != DeliveryStatusEnum.NOT_REGISTERED.CODE
        }.filter { item ->
            if(item.parcel.deliveryStatus == DeliveryStatusEnum.ORPHANED.CODE)
            { //                SopoLog.d("미등록(not_register)[${item.parcelDTO.alias}]")
                multiList[6].add(item)
            }

            item.parcel.deliveryStatus != DeliveryStatusEnum.ORPHANED.CODE
        }.toList()

        multiList[7].addAll(elseList)

        multiList.forEach {
            Collections.sort(it, SortByDate())
            sortedList.addAll(it)
        }
        return sortedList
    }

    fun onSelectAllItemClicked(cb: AppCompatCheckBox)
    {
        _isSelectAllItems.postValue(cb.isChecked.apply {
            SopoLog.d("선택 여부 $this")
        })
    }

    fun onDeleteParcelsClicked() = checkEventStatus(checkNetwork = true) {
        SopoLog.d("onDeleteParcelsClicked() 호출")
        _navigator.postValue(NavigatorEnum.DELETE_PARCEL)
    }

    suspend fun updateParcelToDeleteParcels(deleteParcelIds: List<Int>) =
        withContext(Dispatchers.Default) {
            val parcelStatuses = deleteParcelIds.map { parcelId ->
                parcelManagementRepo.getParcelStatusById(parcelId = parcelId).apply {
                    isBeDelete = 1
                }
            }

            parcelManagementRepo.updateParcelStatuses(parcelStatuses)
        }

    class SortByDate: Comparator<InquiryListItem>
    {
        override fun compare(p0: InquiryListItem, p1: InquiryListItem): Int
        {
            return p0.parcel.auditDte.compareTo(p1.parcel.auditDte)
        }
    }

    override var onSOPOErrorCallback = object: OnSOPOErrorCallback
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
}