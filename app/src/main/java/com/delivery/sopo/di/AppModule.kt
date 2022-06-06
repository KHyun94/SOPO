package com.delivery.sopo.di

import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.shared.SharedPref
import com.delivery.sopo.data.repositories.local.app_password.AppPasswordRepository
import com.delivery.sopo.data.repositories.local.repository.CarrierRepository
import com.delivery.sopo.data.repositories.local.repository.CompletedParcelHistoryRepoImpl
import com.delivery.sopo.data.repositories.local.repository.ParcelManagementRepoImpl
import com.delivery.sopo.data.repositories.local.repository.ParcelRepository
import com.delivery.sopo.data.repositories.local.user.UserLocalRepository
import com.delivery.sopo.data.database.shared.UserSharedPrefHelper
import com.delivery.sopo.data.networks.APIClient
import com.delivery.sopo.data.repositories.parcels.ParcelRepositoryImpl
import com.delivery.sopo.data.repositories.remote.user.UserRemoteRepository
import com.delivery.sopo.data.repositories.user.SignupRepository
import com.delivery.sopo.data.repositories.user.SignupRepositoryImpl
import com.delivery.sopo.data.repositories.user.UserRepository
import com.delivery.sopo.data.repositories.user.UserRepositoryImpl
import com.delivery.sopo.data.resources.auth.local.AuthDataSource
import com.delivery.sopo.data.resources.auth.local.AuthDataSourceImpl
import com.delivery.sopo.data.resources.auth.remote.AuthRemoteDataSource
import com.delivery.sopo.data.resources.auth.remote.AuthRemoteDataSourceImpl
import com.delivery.sopo.data.resources.parcel.local.ParcelDataSource
import com.delivery.sopo.data.resources.parcel.local.ParcelDataSourceImpl
import com.delivery.sopo.data.resources.parcel.local.ParcelStatusDataSource
import com.delivery.sopo.data.resources.parcel.local.ParcelStatusDataSourceImpl
import com.delivery.sopo.data.resources.parcel.remote.ParcelRemoteDataSource
import com.delivery.sopo.data.resources.parcel.remote.ParcelRemoteDataSourceImpl
import com.delivery.sopo.data.resources.user.local.UserDataSource
import com.delivery.sopo.data.resources.user.local.UserDataSourceImpl
import com.delivery.sopo.data.resources.user.remote.SignUpRemoteDataSource
import com.delivery.sopo.data.resources.user.remote.SignUpRemoteDataSourceImpl
import com.delivery.sopo.data.resources.user.remote.UserRemoteDataSource
import com.delivery.sopo.data.resources.user.remote.UserRemoteDataSourceImpl
import com.delivery.sopo.domain.usecase.parcel.local.GetLocalParcelUseCase
import com.delivery.sopo.domain.usecase.parcel.remote.*
import com.delivery.sopo.domain.usecase.user.UpdateFCMTokenUseCase
import com.delivery.sopo.domain.usecase.user.UpdateNicknameUseCase
import com.delivery.sopo.domain.usecase.user.token.*
import com.delivery.sopo.presentation.viewmodels.IntroViewModel
import com.delivery.sopo.presentation.viewmodels.inquiry.*
import com.delivery.sopo.presentation.viewmodels.login.LoginSelectViewModel
import com.delivery.sopo.presentation.viewmodels.login.LoginViewModel
import com.delivery.sopo.presentation.viewmodels.login.ResetPasswordViewModel
import com.delivery.sopo.presentation.viewmodels.main.MainViewModel
import com.delivery.sopo.presentation.viewmodels.menus.*
import com.delivery.sopo.presentation.viewmodels.registesrs.ConfirmParcelViewModel
import com.delivery.sopo.presentation.viewmodels.registesrs.InputParcelViewModel
import com.delivery.sopo.presentation.viewmodels.registesrs.SelectCarrierViewModel
import com.delivery.sopo.presentation.viewmodels.signup.RegisterNicknameViewModel
import com.delivery.sopo.presentation.viewmodels.signup.SignUpCompleteViewModel
import com.delivery.sopo.presentation.viewmodels.signup.SignUpViewModel
import com.delivery.sopo.presentation.viewmodels.splash.SplashViewModel
import kotlinx.coroutines.Dispatchers
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

val dbModule = module {
    single { SharedPref(androidApplication()) }
    single { UserSharedPrefHelper(sharedPref = get(), context = androidApplication()) }

    single { AppDatabase.getInstance(context = get()) }
    single { AppDatabase.getInstance(context = get()).authTok() }
    single { AppDatabase.getInstance(context = get()).carrierDao() }
    single { AppDatabase.getInstance(context = get()).carrierPatternDao() }
    single { AppDatabase.getInstance(context = get()).parcelDao() }
    single { AppDatabase.getInstance(context = get()).completeParcelStatusDao() }
    single { AppDatabase.getInstance(context = get()).parcelManagementDao() }
    single { AppDatabase.getInstance(context = get()).securityDao() }
}

val apiModule = module {
    single { APIClient.provideCache(application = androidApplication()) }
    single { APIClient.getHttpLoggingInterceptor() }
    single { APIClient.getTokenAuthenticator(get(), get()) }

    single(named("PublicOkHttpClient")) { APIClient.providePublicOkHttpClient(cache = get(), loggingInterceptor = get()) }
    single(named("PrivateOkHttpClient")) { APIClient.providePrivateOkHttpClient(cache = get(), loggingInterceptor = get(), authDataSource = get(), authenticator = get()) }

    single(named("PublicAccess")) { APIClient.provideRetrofit(get(named("PublicOkHttpClient"))) }
    single(named("PrivateAccess")) { APIClient.provideRetrofit(get(named("PrivateOkHttpClient"))) }
}

val serviceModule = module {
    single { APIClient.provideParcelService(retrofit = get(named("PrivateAccess"))) }
}

val sourceModule = module {
    single { UserDataSourceImpl(userShared = get()) as UserDataSource }
    single { UserRemoteDataSourceImpl(dispatcher = Dispatchers.IO) as UserRemoteDataSource }

    single { AuthDataSourceImpl(authTokenDao = get(), Dispatchers.Default) as AuthDataSource }
    single { AuthRemoteDataSourceImpl(context = androidApplication(),dispatcher = Dispatchers.IO) as AuthRemoteDataSource }

    single { SignUpRemoteDataSourceImpl(Dispatchers.IO) as SignUpRemoteDataSource }

    single { UserRepositoryImpl(userDataSource = get(), userRemoteDataSource = get(), authDataSource = get(), authRemoteDataSource = get()) as UserRepository }
    single { SignupRepositoryImpl(userDataSource = get(), signUpRemoteDataSource = get()) as SignupRepository}

    single { ParcelDataSourceImpl(parcelDao = get()) as ParcelDataSource }
    single { ParcelRemoteDataSourceImpl(parcelService = get()) as ParcelRemoteDataSource }

    single { ParcelStatusDataSourceImpl(parcelStatusDao = get()) as ParcelStatusDataSource }

    single { ParcelRepositoryImpl(parcelDataSource = get(), parcelStatusDataSource = get(), parcelRemoteDataSource = get()) as com.delivery.sopo.data.repositories.parcels.ParcelRepository }

    single { UserLocalRepository(appDatabase = get(), userShared = get()) }
    single { UserRemoteRepository() }
    single { SignUpRemoteDataSourceImpl(Dispatchers.IO) }
    single { ParcelRepository(get()) }

    single { CarrierRepository(get()) }
    single { ParcelManagementRepoImpl(get()) }
    single { CompletedParcelHistoryRepoImpl(get()) }
    single { AppPasswordRepository(get()) }
}

val useCaseModule = module {

    factory { return@factory SignUpUseCase(signupRepository = get()) }

    factory { return@factory LoginUseCase(userRepository = get()) }
    factory { return@factory ForceLoginUseCase(userRepository = get()) }

    factory { return@factory UpdateNicknameUseCase(userRepository = get()) }
    factory { return@factory UpdateFCMTokenUseCase(userRepository = get()) }
    factory { return@factory GetParcelUseCase(parcelRepo = get(), parcelStatusRepo = get()) }
    factory { SyncParcelsUseCase(parcelRepo = get(), parcelStatusRepo = get()) }
    factory { UpdateParcelsUseCase(get(), get()) }
    factory { RegisterParcelUseCase(parcelRepository = get()) }
    factory { GetCompleteParcelUseCase(get(), get()) }
    factory { GetCompletedMonthUseCase(get(), get()) }
    factory { UpdateParcelUseCase(get()) }
    factory { RefreshParcelsUseCase(get()) }

    factory { UpdateParcelAliasUseCase(get()) }
    factory { DeleteParcelsUseCase(get(), get()) }

    factory { LogoutUseCase(get()) }
    factory { return@factory SignOutUseCase(userRepository = get()) }
    factory { GetLocalParcelUseCase(get()) }

}

val viewModelModule = module {
    viewModel { SplashViewModel(forceLoginUseCase = get(), userDataSource = get()) }

    viewModel { IntroViewModel() }

    viewModel { LoginViewModel(get()) }
    viewModel { return@viewModel SignUpViewModel(signUpUseCase = get()) }
    viewModel { SignUpCompleteViewModel(get(), get()) }
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
    viewModel { InquiryViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { DeleteParcelViewModel(get(), get(), get(), get(), get()) }
    viewModel { MenuViewModel(get()) }
    viewModel { AccountManagerViewModel(get()) }
    viewModel { SignOutViewModel(get()) }
    viewModel { UpdateNicknameViewModel(get(), get(), get()) }

    viewModel { InquiryMainViewModel() }
    viewModel { OngoingTypeViewModel(get(), get(), get(), get(), get()) }
    viewModel { CompletedTypeViewModel(get(), get(), get(), get()) }
    viewModel { MenuMainViewModel() }
    viewModel { ParcelDetailViewModel(get(), get()) }

    viewModel { InputParcelViewModel(get()) }
    viewModel { SelectCarrierViewModel(get()) }
    viewModel { ConfirmParcelViewModel(get()) }
}