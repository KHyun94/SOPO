package com.delivery.sopo.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.delivery.sopo.data.repository.local.repository.AppPasswordRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repository.local.repository.ParcelRepoImpl
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.viewmodels.main.MainViewModel

class MainViewModelFactory(private val userLocalRepository: UserLocalRepository, private val parcelRepoImpl: ParcelRepoImpl, private val parcelManagementRepoImpl: ParcelManagementRepoImpl, private val appPasswordRepoImpl: AppPasswordRepoImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            MainViewModel(
                userLocalRepository,
                parcelRepoImpl,
                parcelManagementRepoImpl,
                appPasswordRepoImpl
            ) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}