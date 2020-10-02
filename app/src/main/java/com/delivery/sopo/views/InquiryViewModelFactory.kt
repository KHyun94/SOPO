package com.delivery.sopo.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.delivery.sopo.repository.ParcelManagementRepoImpl
import com.delivery.sopo.repository.ParcelRepoImpl
import com.delivery.sopo.repository.shared.UserRepo
import com.delivery.sopo.viewmodels.MainViewModel
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel

class InquiryViewModelFactory(private val userRepo: UserRepo, private val parcelRepoImpl: ParcelRepoImpl, private val parcelManagementRepoImpl: ParcelManagementRepoImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(InquiryViewModel::class.java)) {
            InquiryViewModel(userRepo, parcelRepoImpl, parcelManagementRepoImpl) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}