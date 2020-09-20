package com.delivery.sopo.viewmodels.menus

import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.ViewModel
import com.delivery.sopo.repository.shared.UserRepo

class MenuViewModel(private val userRepo: UserRepo) : ViewModel(), LifecycleObserver
{
}