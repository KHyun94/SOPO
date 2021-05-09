package com.delivery.sopo.data.repository.database.shared

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

class SharedPref(context: Context) {

    private var pref: SharedPreferences? = null
    private var editor: SharedPreferences.Editor? = null
    private val PREF_NAME = "USER-INFO"

    init {
        if (pref == null) {
            pref = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE)
        }
        if (editor == null) {
            editor = pref!!.edit()
        }
    }

    fun getString(key: String, value: String): String? {
        return pref!!.getString(key, value)
    }

    fun setString(key: String, value: String) {
        editor!!.putString(key, value)
        editor!!.apply()
    }

    fun getBoolean(key: String, value: Boolean?): Boolean? {
        return pref!!.getBoolean(key, value!!)
    }

    fun setBoolean(key: String, value: Boolean?) {
        editor!!.putBoolean(key, value!!)
        editor!!.apply()
    }

    fun getInt(key: String, value: Int): Int {
        return pref!!.getInt(key, value)
    }

    fun setInt(key: String, value: Int) {
        editor!!.putInt(key, value)
        editor!!.apply()
    }
}
