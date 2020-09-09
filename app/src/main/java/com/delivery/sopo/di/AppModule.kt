package com.delivery.sopo.di

import com.delivery.sopo.repository.UserRepo
import com.delivery.sopo.database.shared.SharedPref
import com.delivery.sopo.database.shared.SharedPrefHelper
import com.delivery.sopo.viewmodels.*
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import com.delivery.sopo.viewmodels.menus.*
import com.delivery.sopo.viewmodels.registesrs.RegisterStep1ViewModel
import com.delivery.sopo.viewmodels.registesrs.RegisterStep2ViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {

    single {
        SharedPref(androidApplication())
    }

    single {
        SharedPrefHelper(
            get(),
            androidApplication()
        )
    }

    single {
        UserRepo(get())
    }

    viewModel { SplashViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { SignUpViewModel() }
    viewModel { LoginSelectViewModel() }
    viewModel { MyMenuViewModel(get()) }
    viewModel { MainViewModel(get()) }
    viewModel { LockScreenViewModel() }
    viewModel { SettingViewModel(get()) }
    viewModel { NoticeViewModel() }
    viewModel { NotDisturbTimeViewModel() }
    viewModel { InquiryViewModel(get()) }

    // merge할 때 지우고 붙여넣어야함
    viewModel { RegisterStep1ViewModel() }
    viewModel { RegisterStep2ViewModel() }
}