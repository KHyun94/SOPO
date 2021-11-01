package com.delivery.sopo.di

import android.os.Parcel
import com.delivery.sopo.data.repository.database.room.AppDatabase
import com.delivery.sopo.data.repository.database.shared.SharedPref
import com.delivery.sopo.data.repository.local.app_password.AppPasswordRepository
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserSharedPrefHelper
import com.delivery.sopo.data.repository.local.repository.*
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.networks.repository.JoinRepositoryImpl
import com.delivery.sopo.util.ui_util.CustomProgressBar
import com.delivery.sopo.viewmodels.IntroViewModel
import com.delivery.sopo.viewmodels.inquiry.InquiryMainViewModel
import com.delivery.sopo.viewmodels.inquiry.InquiryViewModel
import com.delivery.sopo.viewmodels.inquiry.ParcelDetailViewModel
import com.delivery.sopo.viewmodels.login.LoginSelectViewModel
import com.delivery.sopo.viewmodels.login.LoginViewModel
import com.delivery.sopo.viewmodels.login.ResetPasswordViewModel
import com.delivery.sopo.viewmodels.main.MainViewModel
import com.delivery.sopo.viewmodels.menus.*
import com.delivery.sopo.viewmodels.registesrs.InputParcelViewModel
import com.delivery.sopo.viewmodels.registesrs.SelectCarrierViewModel
import com.delivery.sopo.viewmodels.registesrs.ConfirmParcelViewModel
import com.delivery.sopo.viewmodels.signup.SignUpCompleteViewModel
import com.delivery.sopo.viewmodels.signup.RegisterNicknameViewModel
import com.delivery.sopo.viewmodels.signup.SignUpViewModel
import com.delivery.sopo.viewmodels.splash.SplashViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module


val appModule = module {

    single { SharedPref(androidApplication()) }
    single {
        UserSharedPrefHelper(
            get(), androidApplication()
        )
    }

    single { UserLocalRepository(get()) }
    factory { UserRemoteRepository() }
    factory { JoinRepositoryImpl() }
    factory { ParcelRepository(get(), get(),get()) }
    single { AppDatabase.getInstance(get()) }
    single { CarrierRepository(get()) }
    single { ParcelManagementRepoImpl(get()) }
    single { CompletedParcelHistoryRepoImpl(get()) }
    single { AppPasswordRepository(get()) }
    single { OAuthLocalRepository(get()) }

//    factory { LoginBySelfUseCase(get(), get()) }

    viewModel { SplashViewModel(get(), get(), get()) }
    viewModel { IntroViewModel() }
    viewModel { LoginViewModel(get()) }
    viewModel { SignUpViewModel(get(), get()) }
    viewModel { SignUpCompleteViewModel(get(), get()) }
    viewModel { RegisterNicknameViewModel(get()) }
    viewModel { LoginSelectViewModel(get(),get()) }
    viewModel { ResetPasswordViewModel(get()) }
    viewModel { MainViewModel(get(), get()) }
    viewModel { MenuSubViewModel() }
    viewModel { LockScreenViewModel(get(), get(), get()) }
    viewModel { SettingViewModel(get()) }
    viewModel { NoticeViewModel() }
    viewModel { FaqViewModel() }
    viewModel { AppInfoViewModel() }
    viewModel { NotDisturbTimeViewModel() }
    viewModel { InquiryViewModel(get(), get(), get()) }
    viewModel { MenuViewModel(get()) }
    viewModel { AccountManagerViewModel() }
    viewModel { SignOutViewModel(get()) }
    viewModel { UpdateNicknameViewModel(get(), get()) }

    viewModel { InquiryMainViewModel() }
    viewModel { MenuMainViewModel() }
    viewModel { ParcelDetailViewModel(get(), get(), get()) }

    viewModel { InputParcelViewModel(get()) }
    viewModel { SelectCarrierViewModel(get()) }
    viewModel { ConfirmParcelViewModel(get()) }
}