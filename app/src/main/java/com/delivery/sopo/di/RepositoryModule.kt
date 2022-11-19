package com.delivery.sopo.di

import android.content.Context
import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.shared.UserSharedPrefHelper
import com.delivery.sopo.data.networks.serivces.ParcelService
import com.delivery.sopo.data.networks.serivces.UserService
import com.delivery.sopo.data.repositories.local.datasource.CompleteParcelStatusRepository
import com.delivery.sopo.data.repositories.local.repository.CarrierDataSource
import com.delivery.sopo.data.repositories.local.repository.CompletedParcelHistoryRepoImpl
import com.delivery.sopo.data.repositories.parcels.ParcelRepository
import com.delivery.sopo.data.repositories.parcels.ParcelRepositoryImpl
import com.delivery.sopo.data.repositories.user.SignupRepository
import com.delivery.sopo.data.repositories.user.SignupRepositoryImpl
import com.delivery.sopo.data.repositories.user.UserRepository
import com.delivery.sopo.data.repositories.user.UserRepositoryImpl
import com.delivery.sopo.data.resources.auth.local.AuthDataSource
import com.delivery.sopo.data.resources.auth.remote.AuthRemoteDataSource
import com.delivery.sopo.data.resources.auth.remote.AuthRemoteDataSourceImpl
import com.delivery.sopo.data.resources.parcel.local.ParcelDataSource
import com.delivery.sopo.data.resources.parcel.local.ParcelStatusDataSource
import com.delivery.sopo.data.resources.parcel.remote.ParcelRemoteDataSource
import com.delivery.sopo.data.resources.user.local.UserDataSource
import com.delivery.sopo.data.resources.user.local.UserDataSourceImpl
import com.delivery.sopo.data.resources.user.remote.SignUpRemoteDataSource
import com.delivery.sopo.data.resources.user.remote.UserRemoteDataSource
import com.delivery.sopo.data.resources.user.remote.UserRemoteDataSourceImpl
import com.google.firebase.crashlytics.internal.common.AppData
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule
{
    @Provides
    @Singleton
    fun provideUserRepository(authDataSource: AuthDataSource, authRemoteDataSource: AuthRemoteDataSource, userDataSource: UserDataSource, userRemoteDataSource: UserRemoteDataSource): UserRepository
    {
        return UserRepositoryImpl(authDataSource = authDataSource, authRemoteDataSource = authRemoteDataSource, userDataSource = userDataSource, userRemoteDataSource = userRemoteDataSource)
    }
    @Provides
    @Singleton
    fun provideSignUpRepository(userDataSource: UserDataSource, signUpRemoteDataSource: SignUpRemoteDataSource): SignupRepository
    {
        return SignupRepositoryImpl(userDataSource = userDataSource, signUpRemoteDataSource = signUpRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideParcelRepository(carrierDataSource: CarrierDataSource, parcelDataSource: ParcelDataSource, parcelRemoteDataSource: ParcelRemoteDataSource, parcelStatusDataSource: ParcelStatusDataSource): ParcelRepository
    {
        return ParcelRepositoryImpl(carrierDataSource, parcelDataSource, parcelStatusDataSource, parcelRemoteDataSource)
    }

    @Provides
    @Singleton
    fun provideParcelRepositoryTmp(appDatabase: AppDatabase, @PrivateAccess parcelService: ParcelService): com.delivery.sopo.data.repositories.local.repository.ParcelRepository
    {
        return com.delivery.sopo.data.repositories.local.repository.ParcelRepository(appDatabase, parcelService)
    }

    @Provides
    @Singleton
    fun provideCompletedParcelHistoryRepository(appDatabase: AppDatabase): CompleteParcelStatusRepository
    {
        return CompletedParcelHistoryRepoImpl(appDatabase)
    }


    /*

    @Provides
    @Singleton
    fun provideUserDataSource(userSharedPrefHelper: UserSharedPrefHelper): UserDataSource
    {
        return UserDataSourceImpl(userSharedPrefHelper)
    }

    @Provides
    @Singleton
    fun provideUserRemoteDataSource(@PublicAccess userPubService: UserService, @PrivateAccess userPriService: UserService): UserRemoteDataSource
    {
        return UserRemoteDataSourceImpl(userPubService = userPubService, userPriService = userPriService)
    }*/

}