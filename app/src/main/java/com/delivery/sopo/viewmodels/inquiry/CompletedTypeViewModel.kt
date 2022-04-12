package com.delivery.sopo.viewmodels.inquiry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.delivery.sopo.exceptions.ParcelExceptionHandler
import com.delivery.sopo.data.database.room.dto.CompletedParcelHistory
import com.delivery.sopo.data.repository.local.repository.CompletedParcelHistoryRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepository
import com.delivery.sopo.enums.ErrorEnum
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.SelectItem
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.inquiry.PagingManagement
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.usecase.parcel.remote.GetCompleteParcelUseCase
import com.delivery.sopo.usecase.parcel.remote.GetCompletedMonthUseCase
import com.delivery.sopo.usecase.parcel.remote.UpdateParcelAliasUseCase
import com.delivery.sopo.util.DateUtil
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*

class CompletedTypeViewModel(private val getCompleteParcelUseCase: GetCompleteParcelUseCase, private val getCompletedMonthUseCase: GetCompletedMonthUseCase, private val updateParcelAliasUseCase: UpdateParcelAliasUseCase, private val historyRepo: CompletedParcelHistoryRepoImpl):
        BaseViewModel()
{
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

    val yearOfCalendar = MutableLiveData<String>().apply { postValue(DateUtil.getCurrentYear()) }
    val monthsOfCalendar = MutableLiveData<List<SelectItem<CompletedParcelHistory>>>()
    val selectedDate = MutableLiveData<String>()

    private var pagingManagement: PagingManagement

    fun initPage()
    {
        pagingManagement = PagingManagement(0, "", true)
    }

    init
    {
        pagingManagement = PagingManagement(0, "", true)
        getRemoteCompletedMonth().start()
    }

    fun changeCompletedParcelHistoryDate(year: String)
    {
        pagingManagement = PagingManagement(0, "", true)
        yearOfCalendar.postValue(year)
        updateMonthsSelector(year)
    }

    fun getRemoteCompletedMonth() = scope.launch(coroutineExceptionHandler) {
        getCompletedMonthUseCase.invoke()
    }

    fun updateCompletedParcelCalendar(year: String)
    {
        updateYearSpinner(year = year)
        updateMonthsSelector(year = year)
    }

    private fun updateYearSpinner(year: String)
    {
        SopoLog.i("호출 [data:$year]")
        yearOfCalendar.postValue(year)
    }

    // UI를 통해 사용자가 배송완료에서 조회하고 싶은 년월을 바꾼다.
    private fun updateMonthsSelector(year: String) = scope.launch(Dispatchers.Default) {

        SopoLog.i("호출 [data:$year]")

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

                SelectItem(item = it, isSelect = isSelected)
            }
        }
        monthsOfCalendar.postValue(histories)
    }

    fun refreshCompleteParcelsByDate(inquiryDate: String) =
        scope.launch(coroutineExceptionHandler) {
            val list = getCompleteParcelsWithPaging(inquiryDate = inquiryDate).map { parcel ->
                InquiryListItem(parcel, false)
            }.toMutableList()

            _completeList.postValue(list)
        }


    // 배송완료 리스트를 가져온다.(페이징 포함)
    suspend fun getCompleteParcelsWithPaging(inquiryDate: String): List<Parcel.Common>
    {
        SopoLog.i("호출 [date:$inquiryDate]")

        if(pagingManagement.inquiryDate != inquiryDate)
        {
            SopoLog.d("페이징 초기화")
            pagingManagement = PagingManagement(0, inquiryDate, true)
        }

        if(!pagingManagement.hasNext)
        {
            SopoLog.d("다음 페이지가 없습니다. [data:${pagingManagement.toString()}]")
            return emptyList()
        }

        val nextPageParcels = getCompleteParcelUseCase.invoke(pagingManagement)

        // nextPage의 갯수가 10개 미만 일 경우
        if(nextPageParcels.size < 10)
        {
            pagingManagement.hasNext = false
        }
        else
        {
            pagingManagement.pagingNum += 1
        }

        return nextPageParcels
    }

    fun onMonthClicked(month: Int)
    {
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
            scope.launch(coroutineExceptionHandler) {
                updateParcelAliasUseCase.invoke(parcelId = parcelId, parcelAlias = parcelAlias)

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

        override fun onDuplicateError(error: ErrorEnum)
        {
            super.onDuplicateError(error)
            moveDuplicated()
        }
    }
}