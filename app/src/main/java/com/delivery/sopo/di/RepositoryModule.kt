package com.delivery.sopo.di

import android.content.Context
import com.delivery.sopo.data.database.shared.UserSharedPrefHelper
import com.delivery.sopo.data.networks.serivces.UserService
import com.delivery.sopo.data.repositories.user.SignupRepository
import com.delivery.sopo.data.repositories.user.SignupRepositoryImpl
import com.delivery.sopo.data.repositories.user.UserRepository
import com.delivery.sopo.data.repositories.user.UserRepositoryImpl
import com.delivery.sopo.data.resources.auth.local.AuthDataSource
import com.delivery.sopo.data.resources.auth.remote.AuthRemoteDataSource
import com.delivery.sopo.data.resources.auth.remote.AuthRemoteDataSourceImpl
import com.delivery.sopo.data.resources.user.local.UserDataSource
import com.delivery.sopo.data.resources.user.local.UserDataSourceImpl
import com.delivery.sopo.data.resources.user.remote.SignUpRemoteDataSource
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