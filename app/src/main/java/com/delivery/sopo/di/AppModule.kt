package com.delivery.sopo.di

import com.delivery.sopo.networks.NetworkManager
import com.delivery.sopo.repository.UserRepo
import com.delivery.sopo.shared.SharedPref
import com.delivery.sopo.shared.SharedPrefHelper
import com.delivery.sopo.viewmodels.*
import com.delivery.sopo.viewmodels.registesrs.RegisterViewModel
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
    viewModel { MainViewModel() }
    // merge할 때 지우고 붙여넣어야함
    viewModel { RegisterViewModel() }
}