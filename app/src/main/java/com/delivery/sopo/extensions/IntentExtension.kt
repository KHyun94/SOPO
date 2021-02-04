package com.delivery.sopo.extensions

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

fun Intent.launchActivity(context : Context){
    context.startActivity(this)
}