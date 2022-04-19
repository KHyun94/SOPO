package com.delivery.sopo.di

import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.shared.SharedPref
import com.delivery.sopo.data.repository.local.app_password.AppPasswordRepository
import com.delivery.sopo.data.repository.local.datasource.ParcelManagementRepository
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserSharedPrefHelper
import com.delivery.sopo.data.repository.local.repository.*
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.networks.repository.JoinRepositoryImpl
import com.delivery.sopo.usecase.LogoutUseCase
import com.delivery.sopo.usecase.UpdateNicknameUseCase
import com.delivery.sopo.usecase.parcel.local.GetLocalParcelUseCase
import com.delivery.sopo.usecase.parcel.remote.*
import com.delivery.sopo.usecase.user.LoginUseCase
import com.delivery.sopo.usecase.user.SignOutUseCase
import com.delivery.sopo.usecase.user.SignUpUseCase
import com.delivery.sopo.viewmodels.IntroViewModel
import com.delivery.sopo.viewmodels.inquiry.*
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
import org.koin.dsl.bind
import org.koin.dsl.module


val appModule = module {

    single { SharedPref(androidApplication()) }
    single { UserSharedPrefHelper(get(), androidApplication()) }
    single { UserLocalRepository(appDatabase = get(), userShared = get()) }
    single { UserRemoteRepository() }
    single { JoinRepositoryImpl() }
    single { ParcelRepository(get(), get()) }
    single { AppDatabase.getInstance(get()) }
    single { CarrierRepository(get()) }
    single<ParcelManagementRepository> { ParcelManagementRepoImpl(get()) } bind ParcelManagementRepository::class
    single { CompletedParcelHistoryRepoImpl(get()) }
    single { AppPasswordRepository(get()) }
    single { OAuthLocalRepository(get()) }

    factory { return@factory GetParcelUseCase(parcelRepo = get(), parcelStatusRepo = get()) }
    factory { SyncParcelsUseCase(parcelRepo = get(), parcelStatusRepo = get()) }
    factory { UpdateParcelsUseCase(get(), get()) }
    factory { RegisterParcelUseCase(get()) }
    factory { GetCompleteParcelUseCase(get()) }
    factory { GetCompletedMonthUseCase(get(), get())}
    factory { RefreshParcelUseCase(get()) }
    factory { RefreshParcelsUseCase(get()) }
    factory { UpdateNicknameUseCase(get(), get()) }
    factory { UpdateParcelAliasUseCase(get()) }
    factory { DeleteParcelsUseCase(get(), get()) }
    factory { return@factory LoginUseCase(userRemoteRepo = get()) }
    factory { LogoutUseCase(get()) }
    factory { return@factory SignOutUseCase(userLocalRepo = get(), userRemoteRepo = get()) }
    factory { GetLocalParcelUseCase(get()) }
    factory { return@factory SignUpUseCase(userLocalRepo = get(), joinRepo = get()) }

    viewModel { SplashViewModel(get(), get(), get(), get()) }
    viewModel { IntroViewModel() }
    viewModel { LoginViewModel(get()) }
    viewModel { return@viewModel SignUpViewModel(signUpUseCase = get()) }
    viewModel { SignUpCompleteViewModel(get(), get()) }
    viewModel { RegisterNicknameViewModel(get()) }
    viewModel { return@viewModel LoginSelectViewModel(loginUseCase = get(), signUpUseCase = get()) }
    viewModel { ResetPasswordViewModel(get()) }
    viewModel { MainViewModel(get(), get(), get()) }
    viewModel { MenuSubViewModel() }
    viewModel { LockScreenViewModel(get(), get()) }
    viewModel { SettingViewModel(get(), get()) }
    viewModel { NoticeViewModel() }
    viewModel { FaqViewModel() }
    viewModel { AppInfoViewModel() }
    viewModel { InquiryViewModel(get(), get(), get(), get(),get(), get()) }
    viewModel { DeleteParcelViewModel(get(), get(), get(), get(), get()) }
    viewModel { MenuViewModel(get()) }
    viewModel { AccountManagerViewModel(get()) }
    viewModel { SignOutViewModel(get()) }
    viewModel { UpdateNicknameViewModel(get(), get(), get()) }

    viewModel { InquiryMainViewModel() }
    viewModel { OngoingTypeViewModel(get(), get(),get(),get(),get(),get()) }
    viewModel { CompletedTypeViewModel(get(), get(), get(), get()) }
    viewModel { MenuMainViewModel() }
    viewModel { ParcelDetailViewModel(get(), get(), get(),get(), get()) }

    viewModel { InputParcelViewModel(get()) }
    viewModel { SelectCarrierViewModel(get()) }
    viewModel { ConfirmParcelViewModel(get()) }
}