package com.delivery.sopo.presentation.viewmodels.inquiry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.data.database.room.dto.DeliveredParcelHistory
import com.delivery.sopo.data.models.Result
import com.delivery.sopo.data.repositories.local.repository.CompletedParcelHistoryRepoImpl
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.inquiry.PagingManagement
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.domain.usecase.parcel.remote.GetCompleteParcelUseCase
import com.delivery.sopo.domain.usecase.parcel.remote.GetCompletedMonthUseCase
import com.delivery.sopo.domain.usecase.parcel.remote.UpdateParcelAliasUseCase
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class CompletedTypeViewModel(private val getCompleteParcelUseCase: GetCompleteParcelUseCase, private val getCompletedMonthUseCase: GetCompletedMonthUseCase, private val updateParcelAliasUseCase: UpdateParcelAliasUseCase, private val historyRepo: CompletedParcelHistoryRepoImpl):
        BaseViewModel()
{
    /**
     * 완료된 택배 페이지
     */
    private var _completeList = MutableLiveData<MutableList<InquiryListItem>>()
    val completeList: LiveData<MutableList<InquiryListItem>> = _completeList

    /*val completeList = mutableListOf<InquiryListItem>()*/

    private val _completedParcels: MutableStateFlow<Result<List<InquiryListItem>>> = MutableStateFlow(Result.Uninitialized)
    val completedParcels = _completedParcels.asStateFlow()

    // 배송완료 조회 가능한 'Calendar'
    val histories: LiveData<List<DeliveredParcelHistory>>
        get() = historyRepo.getAllAsLiveData()

    var isMonthClickable: Boolean = true

    val yearOfCalendar = MutableLiveData<String>().apply { postValue(DateUtil.getCurrentYear()) }
    val monthsOfCalendar = MutableLiveData<List<SelectItem<DeliveredParcelHistory>>>()
    val selectedDate = MutableLiveData<String>()

    private lateinit var pagingManagement: PagingManagement

    private var isUpdating: Boolean = false

    init
    {
        initPage()
        getActivateMonths()
    }

    fun initPage()
    {
        pagingManagement = PagingManagement.init()
    }

    fun changeCompletedParcelHistoryDate(year: String)
    {
        initPage()
        updateYearSpinner(year)
        updateMonthsSelector(year)
    }

    fun getActivateMonths() = scope.launch {
        getCompletedMonthUseCase()
    }

    fun updateCompletedParcelCalendar(year: String)
    {
        updateYearSpinner(year = year)
        updateMonthsSelector(year = year)
    }

    private fun updateYearSpinner(year: String)
    {
        yearOfCalendar.postValue(year)
    }

    // UI를 통해 사용자가 배송완료에서 조회하고 싶은 년월을 바꾼다.
    private fun updateMonthsSelector(year: String) = scope.launch(Dispatchers.Default) {

        var isLastMonth = false

        val histories = historyRepo.findById("${year}%").map {

            val isSelected = if(it.count > 0 && !isLastMonth)
            {
                isLastMonth = true
                true
            }
            else
            {
                false
            }

            SelectItem(item = it, isSelect = isSelected)
        }
        monthsOfCalendar.postValue(histories)
    }

/*    fun refreshCompleteParcelsByDate(inquiryDate: String) = CoroutineScope(Dispatchers.IO).launch {

        if(isUpdating) return@launch

        flow<Result<List<InquiryListItem>>> {
            val list = getCompleteParcelsWithPaging(inquiryDate = inquiryDate).map { parcel -> InquiryListItem(parcel, false) }.toMutableList()

            if(pagingManagement.pagingNum <= 1)
            {
                completeList.clear()
                completeList.addAll(list) //            _completeList.postValue(list)
            }
            else
            {*//*val li = _completeList.value?.plus(list)?: emptyList<InquiryListItem>()
                _completeList.postValue(li.toMutableList())*//*

                completeList.addAll(list) //            _completeList.postValue(li.toMutableList())
            }

            SopoLog.d("Parcels 1차(${completeList.size}) ${completeList.map { it.parcel }.joinToString()}")

            emit(Result.Success(completeList))

        }.catch { e -> emit(Result.Error(e)) }.collect {

            _completedParcels.value = it
            SopoLog.d("1.5차 ${it.toString()}")
        }
    }*/

        fun refreshCompleteParcelsByDate(inquiryDate: String) = scope.launch {

            if(isUpdating) return@launch

            val list = getCompleteParcelsWithPaging(inquiryDate = inquiryDate).map { parcel ->
                InquiryListItem(parcel, false)
            }.toMutableList()

            if(pagingManagement.pagingNum <= 1) _completeList.postValue(list)
            else {
                val li = _completeList.value?.plus(list)?: emptyList<InquiryListItem>()

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

    fun onMonthClicked(month: Int)
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

    fun updateParcelAlias(parcelId: Int, parcelAlias: String) =
        checkEventStatus(checkNetwork = true) {
            scope.launch {
                updateParcelAliasUseCase.invoke(parcelId = parcelId, parcelAlias = parcelAlias)
            }
        }

    override fun handlerAPIException(exception: SOPOApiException)
    {
        super.handlerAPIException(exception)
        when(exception.code)
        {
            ErrorCode.VALIDATION -> postErrorSnackBar(exception.message)
            ErrorCode.ALREADY_REGISTERED_PARCEL, ErrorCode.OVER_REGISTERED_PARCEL, ErrorCode.PARCEL_BAD_REQUEST -> postErrorSnackBar(exception.message)
            else ->
            {
                exception.printStackTrace()
                postErrorSnackBar("[불명]${exception.message}")
            }
        }
    }

    override fun handlerInternalServerException(exception: InternalServerException)
    {
        super.handlerInternalServerException(exception)

        postErrorSnackBar("서버 오류로 인해 정상적인 처리가 되지 않았습니다.")
    }

    override fun handlerException(exception: Exception)
    {
        super.handlerException(exception)
        postErrorSnackBar("[불명] ${exception.toString()}")
    }
}