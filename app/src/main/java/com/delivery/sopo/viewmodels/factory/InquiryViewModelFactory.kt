package com.delivery.sopo.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.delivery.sopo.repository.impl.ParcelManagementRepoImpl
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.TimeCountRepoImpl
import com.delivery.sopo.repository.shared.UserRepo
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel

class InquiryViewModelFactory(private val userRepo: UserRepo,
                              private val parcelRepoImpl: ParcelRepoImpl,
                              private val parcelManagementRepoImpl: ParcelManagementRepoImpl,
                              private val timeCountRepoImpl: TimeCountRepoImpl
) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(InquiryViewModel::class.java)) {
            InquiryViewModel(userRepo, parcelRepoImpl, parcelManagementRepoImpl, timeCountRepoImpl) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}