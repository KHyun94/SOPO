package com.delivery.sopo.util

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission

object PermissionUtil
{
    fun isPermissionGranted(context: Context, vararg permissions: String):Boolean{
        var value = false

        for (p in permissions)
        {
            value = ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_DENIED
        }

        return value
    }

    fun permissionCallback(context: Context,  vararg permissions: String, callback: (Boolean) -> Unit)
    {
        val permissionListener = object : PermissionListener
        {
            override fun onPermissionGranted()
            {
                callback.invoke(true)
            }

            override fun onPermissionDenied(deniedPermissions: List<String>)
            {
                callback.invoke(false)
            }
        }

        TedPermission.with(context)
            .setRationaleTitle("SOPO 사용 권한 승인")
            .setPermissionListener(permissionListener)
            .setPermissions(*permissions)
            .check()
    }
}