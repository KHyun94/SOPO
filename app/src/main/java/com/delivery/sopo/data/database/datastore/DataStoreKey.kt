package com.delivery.sopo.data.database.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object DataStoreKey
{
    const val DATASTORE_NAME: String = "sopo_data_store"

    val USER_TOKEN:  Preferences.Key<String> = stringPreferencesKey("USER_TOKEN")
    val USER_NAME:  Preferences.Key<String> = stringPreferencesKey("USER_NAME")
    val USER_PASSWORD:  Preferences.Key<String> = stringPreferencesKey("USER_PASSWORD")

    val DEVICE_INFO:  Preferences.Key<String> = stringPreferencesKey("DEVICE_INFO")

    val JOIN_TYPE :Preferences.Key<String> = stringPreferencesKey("JOIN_TYPE")
    val USER_NICKNAME:Preferences.Key<String> = stringPreferencesKey("USER_NICKNAME")
    val REGISTER_DATE:Preferences.Key<String> = stringPreferencesKey("REGISTER_DATE")

    val STATUS:Preferences.Key<Int> = intPreferencesKey("STATUS")
    val SNS_UID: Preferences.Key<String> = stringPreferencesKey("SNS_UID")
    val PUSH_ALARM_TYPE: Preferences.Key<String> = stringPreferencesKey("PUSH_ALARM_TYPE")
    val APP_PASSWORD: Preferences.Key<String> = stringPreferencesKey("APP_PASSWORD")
    val FCM_TOPIC: Preferences.Key<String> = stringPreferencesKey("FCM_TOPIC")
    val DISTURB_START_TIME: Preferences.Key<String> = stringPreferencesKey("DISTURB_START_TIME")
    val DISTURB_END_TIME : Preferences.Key<String> = stringPreferencesKey("DISTURB_END_TIME")

    val PERSONAL_STATUS_TYPE: Preferences.Key<Int> = intPreferencesKey("PERSONAL_STATUS_TYPE")
    val PERSONAL_STATUS_MESSAGE : Preferences.Key<String> = stringPreferencesKey("PERSONAL_STATUS_MESSAGE")
}