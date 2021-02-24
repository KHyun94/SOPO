package com.delivery.sopo.di

import com.delivery.sopo.database.room.AppDatabase
import com.delivery.sopo.database.shared.SharedPref
import com.delivery.sopo.database.shared.SharedPrefHelper
import com.delivery.sopo.repository.impl.*
import com.delivery.sopo.viewmodels.inquiry.InquiryMainViewModel
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import com.delivery.sopo.viewmodels.inquiry.ParcelDetailViewModel
import com.delivery.sopo.viewmodels.login.LoginSelectViewModel
import com.delivery.sopo.viewmodels.login.LoginViewModel
import com.delivery.sopo.viewmodels.main.MainViewModel
import com.delivery.sopo.viewmodels.menus.*
import com.delivery.sopo.viewmodels.registesrs.RegisterStep1ViewModel
import com.delivery.sopo.viewmodels.registesrs.RegisterStep2ViewModel
import com.delivery.sopo.viewmodels.registesrs.RegisterStep3ViewModel
import com.delivery.sopo.viewmodels.signup.SignUpViewModel
import com.delivery.sopo.viewmodels.splash.SplashViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {

    single { SharedPref(androidApplication()) }
    single {
        SharedPrefHelper(
            get(),
            androidApplication()
        )
    }
    single { UserRepoImpl(get()) }
    single { AppDatabase.getInstance(get()) }
    single { CourierRepolmpl(get()) }
    single { ParcelRepoImpl(get(), get()) }
    single { ParcelManagementRepoImpl(get()) }
    single { TimeCountRepoImpl(get(), get()) }
    single { AppPasswordRepoImpl(get()) }
    single { OauthRepoImpl(get()) }

    viewModel { SplashViewModel(get(), get()) }
    viewModel { LoginViewModel(get()) }
    viewModel { SignUpViewModel() }
    viewModel { LoginSelectViewModel() }
    viewModel { MainViewModel(get(), get(), get(), get()) }
    viewModel { LockScreenViewModel(get(), get()) }
    viewModel { SettingViewModel(get(), get()) }
    viewModel { NoticeViewModel() }
    viewModel { FaqViewModel() }
    viewModel { AppInfoViewModel() }
    viewModel { NotDisturbTimeViewModel() }
    viewModel { InquiryViewModel(get(), get(), get(), get()) }
    viewModel { MenuViewModel(get(), get(), get()) }

    viewModel { InquiryMainViewModel() }
    viewModel { MenuMainViewModel() }
    viewModel { ParcelDetailViewModel(get(), get(), get(), get()) }

    viewModel { RegisterStep1ViewModel() }
    viewModel { RegisterStep2ViewModel(get()) }
    viewModel { RegisterStep3ViewModel(get()) }
}