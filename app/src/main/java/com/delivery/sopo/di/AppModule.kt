package com.delivery.sopo.di

import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.shared.SharedPref
import com.delivery.sopo.data.repository.local.app_password.AppPasswordRepository
import com.delivery.sopo.data.repository.local.o_auth.OAuthLocalRepository
import com.delivery.sopo.data.repository.local.user.UserSharedPrefHelper
import com.delivery.sopo.data.repository.local.repository.*
import com.delivery.sopo.data.repository.local.user.UserLocalRepository
import com.delivery.sopo.data.repository.remote.user.UserRemoteRepository
import com.delivery.sopo.data.networks.repository.JoinRepositoryImpl
import com.delivery.sopo.data.repository.user.UserRepository
import com.delivery.sopo.data.repository.user.UserRepositoryImpl
import com.delivery.sopo.data.resource.user.local.UserDataSource
import com.delivery.sopo.data.resource.user.local.UserDataSourceImpl
import com.delivery.sopo.data.resource.user.remote.UserRemoteDataSource
import com.delivery.sopo.data.resource.user.remote.UserRemoteDataSourceImpl
import com.delivery.sopo.domain.usecase.parcel.local.GetLocalParcelUseCase
import com.delivery.sopo.domain.usecase.parcel.remote.*
import com.delivery.sopo.domain.usecase.user.*
import com.delivery.sopo.domain.usecase.user.token.*
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
import org.koin.dsl.module


val appModule = module {

    single { AppDatabase.getInstance(context = get()) }
    single { AppDatabase.getInstance(context = get()).oauthDao() }
    single { AppDatabase.getInstance(context = get()).carrierDao() }
    single { AppDatabase.getInstance(context = get()).carrierPatternDao() }
    single { AppDatabase.getInstance(context = get()).parcelDao() }
    single { AppDatabase.getInstance(context = get()).completeParcelStatusDao() }
    single { AppDatabase.getInstance(context = get()).parcelManagementDao() }
    single { AppDatabase.getInstance(context = get()).securityDao() }

    single { return@single SharedPref(androidApplication()) }
    single { return@single UserSharedPrefHelper(sharedPref = get(), androidApplication()) }

    single { UserRemoteDataSourceImpl() as UserRemoteDataSource }
    single{ UserDataSourceImpl(userShared = get(), oAuthDao = get()) as UserDataSource }
    single{ UserRepositoryImpl(userDataSource = get(), userRemoteDataSource = get())  as UserRepository}

    single { UserLocalRepository(appDatabase = get(), userShared = get()) }
    single { UserRemoteRepository() }
    single { JoinRepositoryImpl() }
    single { ParcelRepository(get()) }

    single { CarrierRepository(get()) }
    single { ParcelManagementRepoImpl(get()) }
    single { CompletedParcelHistoryRepoImpl(get()) }
    single { AppPasswordRepository(get()) }
    single { OAuthLocalRepository(get()) }

    factory { return@factory LoginUseCase(userRepository = get()) }
    factory { return@factory ForceLoginUseCase(userRepository = get()) }

    factory { return@factory UpdateNicknameUseCase(userRepository = get()) }
    factory { return@factory UpdateFCMTokenUseCase(userRepository = get()) }
    factory { return@factory GetParcelUseCase(parcelRepo = get(), parcelStatusRepo = get()) }
    factory { SyncParcelsUseCase(parcelRepo = get(), parcelStatusRepo = get()) }
    factory { UpdateParcelsUseCase(get(), get()) }
    factory { RegisterParcelUseCase(get()) }
    factory { GetCompleteParcelUseCase(get(), get()) }
    factory { GetCompletedMonthUseCase(get(), get())}
    factory { RefreshParcelUseCase(get()) }
    factory { RefreshParcelsUseCase(get()) }

    factory { UpdateParcelAliasUseCase(get()) }
    factory { DeleteParcelsUseCase(get(), get()) }

    factory { LogoutUseCase(get()) }
    factory { return@factory SignOutUseCase(userRepository = get()) }
    factory { GetLocalParcelUseCase(get()) }
    factory { return@factory SignUpUseCase(userLocalRepo = get(), joinRepo = get()) }

    viewModel { SplashViewModel(forceLoginUseCase = get(), userDataSource = get(), carrierRepo = get()) }

    viewModel { IntroViewModel() }

    viewModel { LoginViewModel(get()) }
    viewModel { return@viewModel SignUpViewModel(signUpUseCase = get()) }
    viewModel { SignUpCompleteViewModel(get(), get(), get()) }
    viewModel { RegisterNicknameViewModel(get()) }
    viewModel { return@viewModel LoginSelectViewModel(loginUseCase = get(), signUpUseCase = get()) }
    viewModel { ResetPasswordViewModel(get()) }
    viewModel { MainViewModel(get(), get(), get(), get()) }
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