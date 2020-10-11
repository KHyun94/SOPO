package com.delivery.sopo.viewmodels.factory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.delivery.sopo.repository.impl.ParcelRepoImpl
import com.delivery.sopo.repository.impl.TimeCountRepoImpl
import com.delivery.sopo.repository.impl.UserRepoImpl
import com.delivery.sopo.viewmodels.menus.MenuViewModel

class MenuViewModelFactory(private val userRepoImpl: UserRepoImpl,
                           private val parcelRepoImpl: ParcelRepoImpl,
                           private val timeCountRepoImpl: TimeCountRepoImpl) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MenuViewModel::class.java)) {
            MenuViewModel(userRepoImpl, parcelRepoImpl, timeCountRepoImpl) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}