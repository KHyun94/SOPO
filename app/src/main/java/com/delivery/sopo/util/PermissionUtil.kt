package com.delivery.sopo.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.delivery.sopo.R
import com.delivery.sopo.consts.PermissionConst
import com.delivery.sopo.interfaces.listener.OnPermissionRequestListener
import com.delivery.sopo.views.dialog.GeneralDialog
import com.delivery.sopo.views.dialog.PermissionDialog
import com.delivery.sopo.views.login.LoginSelectView
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission

object PermissionUtil
{
    fun requestPermission(activity: FragmentActivity, onPermissionRequestListener: OnPermissionRequestListener){
        if(!isPermissionGranted(activity, *PermissionConst.PERMISSION_ARRAY))
        {
            val permissionDialog = PermissionDialog(act = activity) { dialog ->

                permissionCallback(activity, *PermissionConst.PERMISSION_ARRAY) { isGranted ->

                    if(!isGranted)
                    {
                        SopoLog.e("권한 비허가 상태")

                        onPermissionRequestListener.onPermissionDenied()

                        return@permissionCallback
                    }

                    SopoLog.d("권한 허가 상태")

                    onPermissionRequestListener.onPermissionGranted()
                }

                dialog.dismiss()
            }

            permissionDialog.show(activity.supportFragmentManager, "PermissionTag")

            return
        }

        SopoLog.d("권한 허가 상태")
        onPermissionRequestListener.onPermissionGranted()
    }


    private fun isPermissionGranted(context: Context, vararg permissions: String):Boolean{
        var value = false

        for (p in permissions)
        {
            value = ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_DENIED
            if(!value) return false
        }

        return value
    }

    private fun permissionCallback(context: Context, vararg permissions: String, callback: (Boolean) -> Unit)
    {
        val permissionListener = object : PermissionListener
        {
            override fun onPermissionGranted() { callback.invoke(true) }
            override fun onPermissionDenied(deniedPermissions: List<String>) { callback.invoke(false) }
        }

        TedPermission.with(context)
            .setRationaleTitle("SOPO 사용 권한 승인")
            .setPermissionListener(permissionListener)
            .setPermissions(*permissions)
            .check()
    }
}