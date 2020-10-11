package com.delivery.sopo.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.delivery.sopo.repository.impl.AppPasswordRepoImpl
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.shared.UserRepo
import com.delivery.sopo.viewmodels.MainViewModel

class MainViewModelFactory(private val userRepo: UserRepo, private val parcelRepoImpl: ParcelRepoImpl,private val appPasswordRepoImpl: AppPasswordRepoImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            MainViewModel(userRepo, parcelRepoImpl, appPasswordRepoImpl) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}