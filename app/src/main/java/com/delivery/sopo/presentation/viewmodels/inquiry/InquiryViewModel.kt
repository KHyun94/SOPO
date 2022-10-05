package com.delivery.sopo.presentation.viewmodels.inquiry

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.delivery.sopo.presentation.consts.NavigatorConst
import com.delivery.sopo.data.repositories.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.enums.ErrorCode
import com.delivery.sopo.extensions.MutableLiveDataExtension.initialize
import com.delivery.sopo.interfaces.listener.OnSOPOErrorCallback
import com.delivery.sopo.models.base.BaseViewModel
import com.delivery.sopo.models.parcel.Parcel
import com.delivery.sopo.domain.usecase.parcel.remote.*
import com.delivery.sopo.exceptions.InternalServerException
import com.delivery.sopo.exceptions.SOPOApiException
import com.delivery.sopo.util.SopoLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InquiryViewModel @Inject constructor(private val syncParcelsUseCase: SyncParcelsUseCase, private val updateParcelsUseCase: UpdateParcelsUseCase, private val getCompletedMonthUseCase: GetCompletedMonthUseCase, private val refreshParcelsUseCase: RefreshParcelsUseCase, private val deleteParcelsUseCase: DeleteParcelsUseCase, private val parcelManagementRepo: ParcelManagementRepoImpl):
        BaseViewModel()
{
    // '배송중' => '배송완료' 개수
    private val _cntOfBeDelivered = parcelManagementRepo.getIsDeliveredCntLiveData()
    val cntOfBeDelivered: LiveData<Int>
        get() = _cntOfBeDelivered

    private val _isAvailableRefresh = MutableLiveData<Boolean>().initialize(true)
    val isAvailableRefresh: LiveData<Boolean>
        get() = _isAvailableRefresh

    private val _isConfirmDelete: MutableLiveData<Boolean> = MutableLiveData()
    val isConfirmDelete: LiveData<Boolean>
        get() = _isConfirmDelete

    val updatableParcelIds: LiveData<List<Int>>
        get() = parcelManagementRepo.getUpdatableParcelIdsAsLiveData()

    private val _navigator = MutableLiveData<String>()
    val navigator: LiveData<String>
        get() = _navigator

    fun postNavigator(navigator: String)
    {
        _navigator.postValue(navigator)
    }

    fun onUpdateParcels(parcelIds: List<Int>) = scope.launch {
        updateParcelsUseCase.invoke(parcelIds)
        getCompletedMonthUseCase.invoke()
    }

    fun onSyncParcels() = scope.launch {
        syncParcelsUseCase.invoke()
    }

    fun onRefreshParcelsClicked() = scope.launch {
        SopoLog.i("onRefreshParcelsClicked(...) 호출")

        _isAvailableRefresh.postValue(false)

        try
        {
            refreshParcelsUseCase.invoke()
        }
        finally
        {
            Handler(Looper.getMainLooper()).postDelayed(Runnable {
                _isAvailableRefresh.postValue(true)
            }, 3000)

        }
    }

    fun onDeleteParcelsClicked()
    {
        postNavigator(NavigatorConst.TO_DELETE)
    }

    fun startDeleteCount()
    {
        _isConfirmDelete.postValue(true)
    }

    fun stopDeleteCount()
    {
        _isConfirmDelete.postValue(false)
    }

    fun clearDeliveredBadge() = scope.launch(Dispatchers.Default) {
        parcelManagementRepo.updateTotalIsBeDeliveredToZero()
    }

    fun confirmDeleteParcels() = checkEventStatus(checkNetwork = true) {
        scope.launch {

            val parcelStatuses = getDeletableParcelStatuses().apply {
                if(isEmpty()) return@launch
            }

            deleteParcelsUseCase.invoke()
        }
    }

    suspend fun getDeletableParcelStatuses(): List<Parcel.Status>
    {
        return parcelManagementRepo.getDeletableParcelStatuses()
    }

    fun recoverDeleteParcels() = scope.launch(Dispatchers.Default) {
        val parcelStatuses = getDeletableParcelStatuses().map { it.apply { isBeDelete = 0 } }
        parcelManagementRepo.updateParcelStatuses(parcelStatuses)
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