package com.delivery.sopo.extensions

import android.view.Gravity
import android.widget.Toast

fun Toast.topShow()
{
    this.setGravity(Gravity.TOP, 0, 180)
    this.show()
}