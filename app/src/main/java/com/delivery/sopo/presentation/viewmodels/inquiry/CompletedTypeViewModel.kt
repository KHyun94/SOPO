package com.delivery.sopo.presentation.viewmodels.inquiry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.DateSelector
import com.delivery.sopo.data.database.room.dto.DeliveredParcelHistory
import com.delivery.sopo.data.models.Result
import com.delivery.sopo.data.repositories.local.repository.CompletedParcelHistoryRepoImpl
import com.delivery.sopo.data.repositories.parcels.ParcelRepository
import com.delivery.sopo.domain.usecase.parcel.remote.GetCompleteParcelUseCase
import com.delivery.sopo.domain.usecase.parcel.remote.GetCompletedMonthUseCase
import com.delivery.sopo.domain.usecase.parcel.remote.UpdateParcelAliasUseCase
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.inquiry.PagingManagement
import com.delivery.sopo.models.parcel.Parcel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class CompletedTypeViewModel @Inject constructor(
    private val parcelRepo: ParcelRepository,
    private val getCompleteParcelUseCase: GetCompleteParcelUseCase,
    private val getCompletedMonthUseCase: GetCompletedMonthUseCase,
    private val updateParcelAliasUseCase: UpdateParcelAliasUseCase,
    private val historyRepo: CompletedParcelHistoryRepoImpl
) :
    BaseViewModel() {
    private var _completeList = MutableLiveData<MutableList<InquiryListItem>>()
    val completeList: LiveData<MutableList<InquiryListItem>> = _completeList

    private val _completedParcels: MutableStateFlow<Result<List<InquiryListItem>>> =
        MutableStateFlow(Result.Uninitialized)
    val completedParcels = _completedParcels.asStateFlow()

    // 배송완료 조회 가능한 'Calendar'
    val histories: LiveData<List<DeliveredParcelHistory>>
        get() = historyRepo.getAllAsLiveData()

    private var isMonthClickable: Boolean = false

    //    val dateSelector = stateFlowOf<DateSelector>()
    val dateSelector: MutableStateFlow<DateSelector> = MutableStateFlow(DateSelector())
//    var dateSelector: DateSelector? = null

    private var isUpdating: Boolean = false

    init {
        fetchCompletedParcelByMonth()
    }

    suspend fun fetchCompletedMonthInfo(cursorDate: String? = null): DateSelector {
        return parcelRepo.fetchCompletedDateInfo(cursorDate = cursorDate)
    }

    fun fetchCompletedParcelByMonth(cursorDate: String? = null) = scope.launch(Dispatchers.IO) {
        dateSelector.value = fetchCompletedMonthInfo(cursorDate)
        _completeList.postValue(fetchCompletedParcels(dateSelector.value.cursorDate?:return@launch).toMutableList())
    }

    fun onPreviousDate() = scope.launch {
        fetchCompletedParcelByMonth(dateSelector.value.nextDate)
    }

    fun onNextDate() = scope.launch {
        fetchCompletedParcelByMonth(dateSelector.value.previousDate)
    }

    suspend fun fetchCompletedParcels(cursorDate: String): List<InquiryListItem> = viewModelScope.async(Dispatchers.IO) {
        return@async parcelRepo.fetchCompletedParcel(0, cursorDate).map { parcel ->
            InquiryListItem(parcel, false)
        }.toMutableList()
    }.await()

    fun updateParcelAlias(parcelId: Int, parcelAlias: String) =
        checkEventStatus(checkNetwork = true) {
            scope.launch(Dispatchers.IO) {
                updateParcelAliasUseCase.invoke(parcelId = parcelId, parcelAlias = parcelAlias)
            }
        }

    override fun handlerAPIException(exception: SOPOApiException) {
        super.handlerAPIException(exception)
        when (exception.code) {
            ErrorCode.VALIDATION -> postErrorSnackBar(exception.message)
            ErrorCode.ALREADY_REGISTERED_PARCEL, ErrorCode.OVER_REGISTERED_PARCEL, ErrorCode.PARCEL_BAD_REQUEST -> postErrorSnackBar(
                exception.message
            )
            else -> {
                exception.printStackTrace()
                postErrorSnackBar("[불명]${exception.message}")
            }
        }
    }

    override fun handlerInternalServerException(exception: InternalServerException) {
        super.handlerInternalServerException(exception)

        postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
    }

    override fun handlerException(exception: Exception) {
        super.handlerException(exception)
        postErrorSnackBar("[불명] ${exception.toString()}")
    }

/* fun updateCompletedParcelCalendar(year: String)
 {
     updateYearSpinner(year = year)
     updateMonthsSelector(year = year)
 }*/

//    private fun updateYearSpinner(year: String)
//    {
//        yearOfCalendar.postValue(year)
//    }

//    // UI를 통해 사용자가 배송완료에서 조회하고 싶은 년월을 바꾼다.
//    private fun updateMonthsSelector(year: String) = scope.launch(Dispatchers.Default) {
//
//        var isLastMonth = false
//
//        val histories = historyRepo.findById("${year}%").map {
//
//            val isSelected = if(it.count > 0 && !isLastMonth)
//            {
//                isLastMonth = true
//                true
//            }
//            else
//            {
//                false
//            }
//
//            SelectItem(item = it, isSelect = isSelected)
//        }
//        monthsOfCalendar.postValue(histories)
//    }

/*
    fun refreshCompleteParcelsByDate(inquiryDate: String) = scope.launch(Dispatchers.IO) {

        if(isUpdating) return@launch

        val list = getCompleteParcelsWithPaging(inquiryDate = inquiryDate).map { parcel ->
            InquiryListItem(parcel, false)
        }.toMutableList()

        if(pagingManagement.pagingNum <= 1) _completeList.postValue(list)
        else
        {
            val li = _completeList.value?.plus(list) ?: emptyList<InquiryListItem>()

            _completeList.postValue(li.toMutableList())
        }
    }

    // 배송완료 리스트를 가져온다.(페이징 포함)
    suspend fun getCompleteParcelsWithPaging(inquiryDate: String): List<Parcel.Common>
    {
        SopoLog.i("호출 [date:$inquiryDate]")

        isUpdating = true

        if(!pagingManagement.isCheckDate(inquiryDate))
        {
            SopoLog.d("페이징 초기화")
            pagingManagement = PagingManagement(0, inquiryDate, true)
        }

        if(!pagingManagement.hasNext)
        {
            SopoLog.d("다음 페이지가 없습니다. [data:${pagingManagement.toString()}]")
            isUpdating = false
            return emptyList()
        }

        val nextPageParcels = getCompleteParcelUseCase(pagingManagement)

        // nextPage의 갯수가 10개 미만 일 경우
        if(nextPageParcels.size < 10)
        {
            pagingManagement.hasNext = false
        }
        else
        {
            pagingManagement.pagingNum += 1
        }
        SopoLog.d("Next Paging Management ${pagingManagement.toString()}")

        isUpdating = false

        return nextPageParcels
    }
*/

/*fun onMonthClicked(month: Int)
{
    SopoLog.d("onMonthClicked :: $month")

    if(!isMonthClickable) return SopoLog.d("$month 비활성화")

    pagingManagement = PagingManagement(0, "", true)

    val list = monthsOfCalendar.value?.map {
        val selectMonth = month.toString().padStart(2, '0')
        it.isSelect = (selectMonth == it.item.month)
        it
    } ?: return

    monthsOfCalendar.postValue(list)
}
*/



}