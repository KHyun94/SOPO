package com.delivery.sopo.presentation.viewmodels.inquiry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.data.repositories.local.repository.ParcelRepository
import com.delivery.sopo.enums.DeliveryStatusEnum
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.inquiry.InquiryListItem
import com.delivery.sopo.domain.usecase.parcel.remote.DeleteParcelsUseCase
import com.delivery.sopo.domain.usecase.parcel.remote.UpdateParcelUseCase
import com.delivery.sopo.domain.usecase.parcel.remote.SyncParcelsUseCase
import com.delivery.sopo.domain.usecase.parcel.remote.UpdateParcelAliasUseCase
import com.delivery.sopo.util.SopoLog
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.*
import com.delivery.sopo.data.models.Result
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.SOPOApiException
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect

class OngoingTypeViewModel(private val updateParcelUseCase: UpdateParcelUseCase,
                           private val syncParcelsUseCase: SyncParcelsUseCase,
                           private val updateParcelAliasUseCase: UpdateParcelAliasUseCase,
                           private val deleteParcelsUseCase: DeleteParcelsUseCase,
                           private val parcelRepo: ParcelRepository): BaseViewModel()
{
    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    fun postNavigator(navigator: String)
    {
        _navigator.postValue(navigator)
    }

    private val _ongoingParcels = Transformations.map(parcelRepo.getOngoingParcelAsLiveData()) { parcelList ->
        val list = parcelList.map { parcel ->
            SopoLog.d("UPDATE ${parcel.toString()}")
            return@map InquiryListItem(parcel, false)
        }
        sortByDeliveryStatus(list).toMutableList()
    }
    val ongoingParcels: LiveData<MutableList<InquiryListItem>>
        get() = _ongoingParcels

    private val _parcels: MutableStateFlow<Result<List<InquiryListItem>>> = MutableStateFlow(Result.Uninitialized)
    val parcels = _parcels.asStateFlow()

    val cntOfPresentOngoingParcels: LiveData<Int> =
        Transformations.map(parcelRepo.getLocalOnGoingParcelCnt()) { cnt -> cnt }

    init
    {
        syncOngoingParcels()
    }

    fun getOngoingParcels() = CoroutineScope(Dispatchers.Default).launch {
        SopoLog.d("getOngoingParcels(...)")
        parcelRepo.getOngoingParcels().collect { _parcels.value = it }
    }

    /**
     * 이벤트 리스너
     */

    // 서버에서 DB 내 택배 정보를 가져와서 로컬 내 디비 정보를 갱신
    fun syncOngoingParcels() = scope.launch {
        syncParcelsUseCase.invoke()
    }

    fun updateParcelAlias(parcelId: Int, parcelAlias: String) =
        checkEventStatus(checkNetwork = true) {
            scope.launch {
                updateParcelAliasUseCase.invoke(parcelId = parcelId, parcelAlias = parcelAlias)
            }
        }

    suspend fun refreshParcel(parcelId: Int) = withContext(Dispatchers.IO) {
        updateParcelUseCase.invoke(parcelId = parcelId)
    }

    fun deleteParcel(parcelId: Int) = checkEventStatus(checkNetwork = true) {
        scope.launch {
            deleteParcelsUseCase(parcelId = parcelId)
        }
    }

    fun sortByDeliveryStatus(list: List<InquiryListItem>): List<InquiryListItem>
    {
        val sortedList = mutableListOf<InquiryListItem>()
        val multiList = listOf<MutableList<InquiryListItem>>(mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf(), mutableListOf())

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
            {
                multiList[5].add(item)
            }

            item.parcel.deliveryStatus != DeliveryStatusEnum.NOT_REGISTERED.CODE
        }.filter { item ->
            if(item.parcel.deliveryStatus == DeliveryStatusEnum.ORPHANED.CODE)
            {
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

    fun onMoveToRegister()
    {
        SopoLog.d("onMoveToRegister(...) 호출")
        _navigator.postValue(NavigatorConst.MAIN_BRIDGE_REGISTER)
    }

    class SortByDate: Comparator<InquiryListItem>
    {
        override fun compare(p0: InquiryListItem, p1: InquiryListItem): Int
        {
            return p0.parcel.auditDte.compareTo(p1.parcel.auditDte)
        }
    }
    override fun handlerAPIException(exception: SOPOApiException)
    {
        super.handlerAPIException(exception)
        postErrorSnackBar(exception.toString())
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