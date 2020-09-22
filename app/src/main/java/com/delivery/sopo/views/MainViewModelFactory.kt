package com.delivery.sopo.views

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.delivery.sopo.repository.shared.UserRepo
import com.delivery.sopo.viewmodels.MainViewModel

class MainViewModelFactory(private val userRepo: UserRepo) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            MainViewModel(userRepo) as T
        } else {
            throw IllegalArgumentException()
        }
    }
}