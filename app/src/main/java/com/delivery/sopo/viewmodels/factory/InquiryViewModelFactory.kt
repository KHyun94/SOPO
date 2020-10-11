package com.delivery.sopo.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.delivery.sopo.repository.impl.ParcelManagementRepoImpl
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.TimeCountRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel

// todo 나중에 koin으로 주입할 수 있을지 확인할 것.
class InquiryViewModelFactory(private val userRepoImpl: UserRepoImpl,
                              private val parcelRepoImpl: ParcelRepoImpl,
                              private val parcelManagementRepoImpl: ParcelManagementRepoImpl,
                              private val timeCountRepoImpl: TimeCountRepoImpl
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(InquiryViewModel::class.java)) {
            InquiryViewModel(userRepoImpl, parcelRepoImpl, parcelManagementRepoImpl, timeCountRepoImpl) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}