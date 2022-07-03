package com.delivery.sopo.interfaces

import com.delivery.sopo.enums.SnackBarType
import com.delivery.sopo.util.ui_util.BottomNotificationBar

interface OnSnackBarController
{
    fun setSnackBar(bottomNotificationBar: BottomNotificationBar)
    fun getSnackBar(): BottomNotificationBar
    fun onMake(snackBarType: SnackBarType)
    fun onDismiss()
    fun onShow(hasDelay: Boolean = true)
}