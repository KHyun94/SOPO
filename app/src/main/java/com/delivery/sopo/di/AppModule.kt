package com.delivery.sopo.di

import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.repository.shared.UserRepo
import com.delivery.sopo.database.shared.SharedPref
import com.delivery.sopo.database.shared.SharedPrefHelper
import com.delivery.sopo.repository.impl.*
import com.delivery.sopo.viewmodels.*
import com.delivery.sopo.viewmodels.inquiry.InquiryMainViewModel
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import com.delivery.sopo.viewmodels.menus.*
import com.delivery.sopo.viewmodels.registesrs.RegisterStep1ViewModel
import com.delivery.sopo.viewmodels.registesrs.RegisterStep2ViewModel
import com.delivery.sopo.viewmodels.registesrs.RegisterStep3ViewModel
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

    single{
        AppDatabase.getInstance(get())
    }

    single {
        CourierRepolmpl(get())
    }

    single {
        ParcelRepoImpl(get(), get())
    }

    single {
        ParcelManagementRepoImpl(get())
    }

    single {
        TimeCountRepoImpl(get(), get())
    }

    single {
        AppPasswordRepoImpl(get())
    }

    viewModel { SplashViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { SignUpViewModel() }
    viewModel { LoginSelectViewModel() }
    viewModel { MainViewModel(get(),get(), get()) }
    viewModel { LockScreenViewModel(get(),get()) }
    viewModel { SettingViewModel(get(),get()) }
    viewModel { NoticeViewModel()}
    viewModel { FaqViewModel()}
    viewModel { AppInfoViewModel()}
    viewModel { NotDisturbTimeViewModel() }
    viewModel { InquiryViewModel(get(), get(), get(), get()) }
    viewModel { MenuViewModel(get(), get(), get()) }

    viewModel { InquiryMainViewModel() }
    viewModel { ParcelDetailViewModel(get(), get(), get()) }

    viewModel { RegisterStep1ViewModel() }
    viewModel { RegisterStep2ViewModel(get()) }
    viewModel { RegisterStep3ViewModel(get()) }
}