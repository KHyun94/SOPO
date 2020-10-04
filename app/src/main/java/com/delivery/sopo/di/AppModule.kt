package com.delivery.sopo.di

import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.repository.shared.UserRepo
import com.delivery.sopo.database.shared.SharedPref
import com.delivery.sopo.database.shared.SharedPrefHelper
import com.delivery.sopo.repository.CourierRepolmpl
import com.delivery.sopo.repository.ParcelManagementRepoImpl
import com.delivery.sopo.repository.ParcelRepoImpl
import com.delivery.sopo.repository.TimeCountRepoImpl
import com.delivery.sopo.viewmodels.*
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

    viewModel { SplashViewModel(get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { SignUpViewModel() }
    viewModel { LoginSelectViewModel() }
    viewModel { MyMenuViewModel(get()) }
    viewModel { MainViewModel(get()) }
    viewModel { LockScreenViewModel() }
    viewModel { SettingViewModel(get()) }
    viewModel { NoticeViewModel()}
    viewModel { NotDisturbTimeViewModel() }
    viewModel { InquiryViewModel(get(), get(), get(), get()) }
    viewModel { MenuViewModel(get()) }


    viewModel { ParcelDetailViewModel(get(), get()) }

    viewModel { RegisterStep1ViewModel() }
    viewModel { RegisterStep2ViewModel(get()) }
    viewModel { RegisterStep3ViewModel(get()) }
}