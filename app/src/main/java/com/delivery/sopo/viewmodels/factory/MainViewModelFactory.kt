package com.delivery.sopo.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.delivery.sopo.repository.impl.AppPasswordRepoImpl
import com.delivery.sopo.repository.impl.ParcelManagementRepoImpl
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.viewmodels.main.MainViewModel

class MainViewModelFactory(private val userRepoImpl: UserRepoImpl, private val parcelRepoImpl: ParcelRepoImpl, private val parcelManagementRepoImpl: ParcelManagementRepoImpl, private val appPasswordRepoImpl: AppPasswordRepoImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            MainViewModel(
                userRepoImpl,
                parcelRepoImpl,
                parcelManagementRepoImpl,
                appPasswordRepoImpl
            ) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}