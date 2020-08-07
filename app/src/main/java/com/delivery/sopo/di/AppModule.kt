package com.delivery.sopo.di

import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.UserRepo
import com.delivery.sopo.shared.SharedPref
import com.delivery.sopo.shared.SharedPrefHelper
import com.delivery.sopo.viewmodels.LoginSelectViewModel
import com.delivery.sopo.viewmodels.LoginViewModel
import com.delivery.sopo.viewmodels.SignUpViewModel
import com.delivery.sopo.viewmodels.SplashViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {

    single {
        SharedPref(androidApplication())
    }

    single {
        SharedPrefHelper(get(), androidApplication())
    }

    single {
        UserRepo(get())
    }

    viewModel { SplashViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { SignUpViewModel() }
    viewModel { LoginSelectViewModel() }
}