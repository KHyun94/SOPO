package com.delivery.sopo.viewmodels.inquiry

import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.viewModelScope
import com.delivery.sopo.ParcelExceptionHandler
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
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.models.inquiry.PagingManagement
import com.delivery.sopo.models.mapper.ParcelMapper
import com.delivery.sopo.models.parcel.ParcelResponse
import com.delivery.sopo.models.parcel.ParcelStatus
import com.delivery.sopo.usecase.parcel.remote.*
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*
import java.util.*

class OngoingTypeViewModel(
                           private val refreshParcelsUseCase: RefreshParcelsUseCase,
                           private val refreshParcelUseCase: RefreshParcelUseCase,
                           private val syncParcelsUseCase: SyncParcelsUseCase,
                           private val updateParcelAliasUseCase: UpdateParcelAliasUseCase,
                           private val deleteParcelsUseCase: DeleteParcelsUseCase,
                           private val parcelRepo: ParcelRepository,
                           private val parcelManagementRepo: ParcelManagementRepoImpl):
        BaseViewModel()
{
    private val _isAvailableRefresh = MutableLiveData<Boolean>().initialize(true)
    val isAvailableRefresh: LiveData<Boolean>
        get() = _isAvailableRefresh

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

    init
    {
        syncParcelsByOngoing()
    }

    /**
     * 이벤트 리스너
     */

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
        scope.launch(Dispatchers.IO) {
            try
            {
                withContext(Dispatchers.Default) { parcelManagementRepo.delete(parcelId) }
                deleteParcelsUseCase.invoke()
            }
            catch(e: Exception)
            {
                exceptionHandler.handleException(coroutineContext, e)
            }
        }
    }

    fun onDeleteParcels() = checkEventStatus(checkNetwork = true) {
        scope.launch(Dispatchers.IO) {
            try
            {
                deleteParcelsUseCase.invoke()
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

    suspend fun getDeletableParcelStatuses():List<ParcelStatus>{
        return parcelManagementRepo.getDeletableParcelStatuses()
    }

    fun cancelToDelete(parcelStatuses:List<ParcelStatus>) = scope.launch(Dispatchers.Default){
        val updateParcelStatuses = parcelStatuses.map { it.apply { isBeDelete = 0 } }
        parcelManagementRepo.updateParcelStatuses(updateParcelStatuses)
    }

    fun onMoveToRegister()
    {

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
        ParcelExceptionHandler(Dispatchers.Main, onSOPOErrorCallback)
    }
}