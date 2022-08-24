package com.delivery.sopo.di

import android.content.Context
import com.delivery.sopo.data.database.room.dao.AuthTokenDao
import com.delivery.sopo.data.database.shared.UserSharedPrefHelper
import com.delivery.sopo.data.networks.serivces.SignUpService
import com.delivery.sopo.data.networks.serivces.UserService
import com.delivery.sopo.data.resources.auth.local.AuthDataSource
import com.delivery.sopo.data.resources.auth.local.AuthDataSourceImpl
import com.delivery.sopo.data.resources.auth.remote.AuthRemoteDataSource
import com.delivery.sopo.data.resources.auth.remote.AuthRemoteDataSourceImpl
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
        authTokenDao: AuthTokenDao,
        @DefaultDispatcher dispatcher: CoroutineDispatcher
    ): AuthDataSource
    {
        return AuthDataSourceImpl(authTokenDao, dispatcher)
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
    fun provideUserDataSource(userSharedPrefHelper: UserSharedPrefHelper): UserDataSource
    {
        return UserDataSourceImpl(userSharedPrefHelper)
    }

    @Provides
    @Singleton
    fun provideUserRemoteDataSource(
            @PublicAccess userPubService: UserService,
            @PrivateAccess userPriService: UserService): UserRemoteDataSource
    {
        return UserRemoteDataSourceImpl(userPubService = userPubService, userPriService = userPriService)
    }

}