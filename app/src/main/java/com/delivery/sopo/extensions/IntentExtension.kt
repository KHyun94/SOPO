package com.delivery.sopo.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

fun Intent.launchActivity(context : Context){
    context.startActivity(this)
}

fun Intent.launchActivityWithAllClear(context: Context){
    this.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(this)
}

fun Intent.launchActivityForResult(activity: Activity, requestCode: Int){
    activity.startActivityForResult(this, requestCode)
}
