///*
//package com.delivery.sopo.di
//
//import com.delivery.sopo.data.networks.APIClient
//import com.delivery.sopo.data.repositories.local.app_password.AppPasswordRepository
//import com.delivery.sopo.data.repositories.local.repository.CarrierDataSource
//import com.delivery.sopo.data.repositories.local.repository.CompletedParcelHistoryRepoImpl
//import com.delivery.sopo.data.repositories.local.repository.ParcelManagementRepoImpl
//import com.delivery.sopo.data.repositories.local.repository.ParcelRepository
//import com.delivery.sopo.data.repositories.parcels.ParcelRepositoryImpl
//import com.delivery.sopo.data.resources.parcel.local.ParcelDataSource
//import com.delivery.sopo.data.resources.parcel.local.ParcelDataSourceImpl
//import com.delivery.sopo.data.resources.parcel.local.ParcelStatusDataSource
//import com.delivery.sopo.data.resources.parcel.local.ParcelStatusDataSourceImpl
//import com.delivery.sopo.data.resources.parcel.remote.ParcelRemoteDataSource
//import com.delivery.sopo.data.resources.parcel.remote.ParcelRemoteDataSourceImpl
//import com.delivery.sopo.domain.usecase.parcel.local.GetLocalParcelUseCase
//import com.delivery.sopo.domain.usecase.parcel.remote.*
//import com.delivery.sopo.domain.usecase.user.UpdateFCMTokenUseCase
//import com.delivery.sopo.domain.usecase.user.UpdateNicknameUseCase
//import com.delivery.sopo.domain.usecase.user.reset.ResetPasswordUseCase
//import com.delivery.sopo.domain.usecase.user.reset.SendAuthTokenUseCase
//import com.delivery.sopo.domain.usecase.user.reset.VerifyAuthTokenUseCase
//import com.delivery.sopo.domain.usecase.user.token.LogoutUseCase
//import com.delivery.sopo.domain.usecase.user.token.SignOutUseCase
//import com.delivery.sopo.presentation.login.LoginSelectViewModel
//import com.delivery.sopo.presentation.viewmodels.inquiry.*
//import com.delivery.sopo.presentation.viewmodels.login.ResetPasswordViewModel
//import com.delivery.sopo.presentation.viewmodels.main.MainViewModel
//import com.delivery.sopo.presentation.viewmodels.menus.*
//import com.delivery.sopo.presentation.register.viewmodel.ConfirmParcelViewModel
//import com.delivery.sopo.presentation.register.viewmodel.InputParcelViewModel
//import com.delivery.sopo.presentation.register.viewmodel.SelectCarrierViewModel
//import com.delivery.sopo.presentation.viewmodels.signup.RegisterNicknameViewModel
//import kotlinx.coroutines.Dispatchers
//import org.koin.androidx.viewmodel.dsl.viewModel
//import org.koin.core.qualifier.named
//import org.koin.dsl.module
//
//val dbModule = module {
//
//}
//
//val apiModule = module {
//
//}
//
//val serviceModule = module {
//    single { APIClient.provideParcelService(retrofit = get(named("PrivateAccess"))) }
//}
//
//val sourceModule = module {
//
//    single { ParcelDataSourceImpl(parcelDao = get()) as ParcelDataSource }
//    single { ParcelRemoteDataSourceImpl(parcelService = get()) as ParcelRemoteDataSource }
//
//    single { ParcelStatusDataSourceImpl(parcelStatusDao = get()) as ParcelStatusDataSource }
//
//    single { ParcelRepositoryImpl(parcelDataSource = get(), parcelStatusDataSource = get(), parcelRemoteDataSource = get()) as com.delivery.sopo.data.repositories.parcels.ParcelRepository }
//
//    single { ParcelRepository(get()) }
//
//    single { CarrierDataSource(carrierDao = get(), carrierPatternDao = get()) }
//    single { ParcelManagementRepoImpl(get()) }
//    single { CompletedParcelHistoryRepoImpl(get()) }
//    single { AppPasswordRepository(get()) }
//}
//
//val useCaseModule = module {
//
//    factory { return@factory SendAuthTokenUseCase(userRepository = get(), dispatcher = Dispatchers.IO) }
//    factory { return@factory VerifyAuthTokenUseCase(userRepository = get(), dispatcher = Dispatchers.IO) }
//    factory { return@factory ResetPasswordUseCase(userRepository = get(), dispatcher = Dispatchers.IO) }
//
//    factory { return@factory UpdateNicknameUseCase(userRepository = get()) }
//    factory { return@factory UpdateFCMTokenUseCase(userRepository = get()) }
//    factory { return@factory GetParcelUseCase(parcelRepo = get(), parcelStatusRepo = get()) }
//    factory { SyncParcelsUseCase(parcelRepo = get(), parcelStatusRepo = get()) }
//    factory { UpdateParcelsUseCase(get(), get()) }
//    factory { RegisterParcelUseCase(parcelRepository = get()) }
//    factory { GetCompleteParcelUseCase(get(), get()) }
//    factory { GetCompletedMonthUseCase(get(), get()) }
//    factory { UpdateParcelUseCase(get()) }
//    factory { RefreshParcelsUseCase(get()) }
//
//    factory { UpdateParcelAliasUseCase(get()) }
//    factory { DeleteParcelsUseCase(get(), get()) }
//
//    factory { LogoutUseCase(get()) }
//    factory { return@factory SignOutUseCase(userRepository = get()) }
//    factory { GetLocalParcelUseCase(get()) }
//
//}
//
//val viewModelModule = module {
//    viewModel { RegisterNicknameViewModel(updateNicknameUseCase = get()) }
//
//    viewModel { LoginSelectViewModel(loginUseCase = get(), signUpUseCase = get()) }
//    viewModel { ResetPasswordViewModel(sendAuthTokenUseCase = get(), verifyAuthTokenUseCase = get(), resetPasswordUseCase = get()) }
////    viewModel { MainViewModel(get(), get(), get(), get()) }
//    viewModel { MenuSubViewModel() }
//    viewModel { LockScreenViewModel(get(), get()) }
//    viewModel { SettingViewModel(get(), get()) }
//    viewModel { NoticeViewModel() }
//    viewModel { FaqViewModel() }
//    viewModel { AppInfoViewModel() }
//    viewModel { InquiryViewModel(get(), get(), get(), get(), get(), get()) }
//    viewModel { DeleteParcelViewModel(get(), get(), get(), get(), get()) }
//    viewModel { MenuViewModel(get()) }
//    viewModel { AccountManagerViewModel(get(), get()) }
//    viewModel { SignOutViewModel(get()) }
//
//
//    viewModel { InquiryMainViewModel() }
//    viewModel { OngoingTypeViewModel(get(), get(), get(), get(), get()) }
//    viewModel { CompletedTypeViewModel(get(), get(), get(), get()) }
//    viewModel { MenuMainViewModel() }
//    viewModel { ParcelDetailViewModel(get(), get()) }
//
//    viewModel { InputParcelViewModel(get()) }
//    viewModel { SelectCarrierViewModel(get()) }
//    viewModel { ConfirmParcelViewModel(get()) }
//}*/
