package com.delivery.sopo.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.delivery.sopo.data.repository.local.repository.ParcelRepoImpl
import com.delivery.sopo.data.repository.local.repository.TimeCountRepoImpl
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.viewmodels.menus.MenuViewModel

class MenuViewModelFactory(private val userLocalRepository: UserLocalRepository,
                           private val parcelRepoImpl: ParcelRepoImpl,
                           private val timeCountRepoImpl: TimeCountRepoImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
            MenuViewModel(userLocalRepository, parcelRepoImpl, timeCountRepoImpl) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}