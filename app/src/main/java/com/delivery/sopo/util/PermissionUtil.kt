package com.delivery.sopo.util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.delivery.sopo.consts.PermissionConst
import com.delivery.sopo.interfaces.listener.OnPermissionResponseCallback
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission

object PermissionUtil
{
    fun checkNotificationListenerPermission(context: Context, packageName: String): Boolean
    {
        SopoLog.d("requestNotificationListenerPermission(...) 호출 [packageName:$packageName]")
        val sets = NotificationManagerCompat.getEnabledListenerPackages(context)
        return sets.contains(packageName)
    }

    fun requestPermission(activity: FragmentActivity, onPermissionResponseCallback: OnPermissionResponseCallback){
        if(!isPermissionGranted(activity, *PermissionConst.PERMISSION_ARRAY))
        {
            permissionCallback(activity, *PermissionConst.PERMISSION_ARRAY) { isGranted ->

                if(!isGranted)
                {
                    SopoLog.e("권한 비허가 상태")

                    onPermissionResponseCallback.onPermissionDenied()

                    return@permissionCallback
                }

                SopoLog.d("권한 허가 상태")

                onPermissionResponseCallback.onPermissionGranted()
            }

            return
        }

        SopoLog.d("권한 허가 상태")
        onPermissionResponseCallback.onPermissionGranted()
    }


    fun isPermissionGranted(context: Context, vararg permissions: String):Boolean{
        var value = false

        for (p in permissions)
        {
            value = ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_DENIED
            if(!value)
            {
                SopoLog.d("미허가 권한 ${p.toString()}")
                return false
            }
        }

        return value
    }

    fun permissionCallback(context: Context, vararg permissions: String, callback: (Boolean) -> Unit)
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