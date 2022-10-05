package com.delivery.sopo.di

import android.content.Context
import com.delivery.sopo.data.database.datastore.DataStoreManager
import com.delivery.sopo.data.database.room.AppDatabase
import com.delivery.sopo.data.database.room.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DBModule
{
/*
    @Provides
    @Singleton
    fun provideSharedPref(@ApplicationContext context: Context): SharedPref
    {
        return SharedPref(context = context)
    }

    @Provides
    @Singleton
    fun provideUserSharedPrefHelper(sharedPref: SharedPref): UserSharedPrefHelper
    {
        return UserSharedPrefHelper(sharedPref)
    }
*/

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStoreManager
    {
        return DataStoreManager(context)
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase
    {
        return AppDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideCarrierDao(appDatabase: AppDatabase): CarrierDao
    {
        return appDatabase.carrierDao()
    }

    @Provides
    @Singleton
    fun carrierPatternDao(appDatabase: AppDatabase): CarrierPatternDao
    {
        return appDatabase.carrierPatternDao()
    }

    @Provides
    @Singleton
    fun provideParcelDao(appDatabase: AppDatabase): ParcelDao
    {
        return appDatabase.parcelDao()
    }
    @Provides
    @Singleton
    fun provideParcelStatusDao(appDatabase: AppDatabase): ParcelStatusDao
    {
        return appDatabase.parcelStatusDAO()
    }
    @Provides
    @Singleton
    fun provideCompleteParcelStatusDao(appDatabase: AppDatabase): CompleteParcelStatusDao
    {
        return appDatabase.completeParcelStatusDao()
    }
    @Provides
    @Singleton
    fun provideSecurityDao(appDatabase: AppDatabase): AppPasswordDao
    {
        return appDatabase.securityDao()
    }
}