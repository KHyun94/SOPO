package com.delivery.sopo.data.database.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class DataStoreManager @Inject constructor(private val context: Context)
{
    private val Context.sopoDataStore: DataStore<Preferences> by preferencesDataStore(name = DataStoreKey.DATASTORE_NAME)

    suspend fun <T> storeValue(key: Preferences.Key<T>, value: T)
    {
        context.sopoDataStore.edit { it[key] = value }
    }

    suspend fun <T> readValue(key: Preferences.Key<T>) = context.sopoDataStore.data
        .catch { e ->
            if(e is IOException)
            {
                emit(emptyPreferences())
            }
            else
            {
                throw e
            }
        }.map { preferences ->
            preferences[key]
        }.first()


}