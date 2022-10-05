package com.delivery.sopo.di

import android.content.Context
import com.delivery.sopo.data.database.datastore.DataStoreManager
import com.delivery.sopo.data.database.room.dao.ParcelDao
import com.delivery.sopo.data.database.room.dao.ParcelStatusDao
import com.delivery.sopo.data.networks.serivces.ParcelService
import com.delivery.sopo.data.networks.serivces.SignUpService
import com.delivery.sopo.data.networks.serivces.UserService
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
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule
{
    @Provides
    @Singleton
    fun provideSignUpDataSource(
        signUpService: SignUpService,
        @IoDispatcher dispatcher: CoroutineDispatcher
    ): SignUpRemoteDataSource
    {
        return SignUpRemoteDataSourceImpl(signUpService = signUpService, dispatcher = dispatcher)
    }

    @Provides
    @Singleton
    fun provideAuthDataSource(
        dataStoreManager: DataStoreManager,
        @DefaultDispatcher dispatcher: CoroutineDispatcher
    ): AuthDataSource
    {
        return AuthDataSourceImpl(dataStoreManager, dispatcher)
    }

    @Provides
    @Singleton
    fun provideAuthRemoteDataSource(
        @ApplicationContext context: Context,
        @IoDispatcher dispatcher: CoroutineDispatcher,
        @PublicAccess userService: UserService
    ): AuthRemoteDataSource
    {
        return AuthRemoteDataSourceImpl(context, dispatcher, userService)
    }

    @Provides
    @Singleton
    fun provideUserDataSource(dataStoreManager: DataStoreManager, @DefaultDispatcher dispatcher: CoroutineDispatcher): UserDataSource
    {
        return UserDataSourceImpl(dataStoreManager = dataStoreManager, dispatcher = dispatcher)
    }

    @Provides
    @Singleton
    fun provideUserRemoteDataSource(
            @PublicAccess userPubService: UserService,
            @PrivateAccess userPriService: UserService): UserRemoteDataSource
    {
        return UserRemoteDataSourceImpl(userPubService = userPubService, userPriService = userPriService)
    }


    @Provides
    @Singleton
    fun provideParcelDataSource(parcelDao: ParcelDao): ParcelDataSource
    {
        return ParcelDataSourceImpl(parcelDao)
    }


    @Provides
    @Singleton
    fun provideParcelRemoteDataSource(@PrivateAccess parcelService: ParcelService): ParcelRemoteDataSource
    {
        return ParcelRemoteDataSourceImpl(parcelService)
    }


    @Provides
    @Singleton
    fun provideParcelStatusDataSource(parcelStatusDao: ParcelStatusDao): ParcelStatusDataSource
    {
        return ParcelStatusDataSourceImpl(parcelStatusDao)
    }
}