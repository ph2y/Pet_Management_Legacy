package com.sju18001.petmanagement.controller

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.util.ArrayList

class Permission {
    private val REQUEST_CODE = 100

    public val requiredPermissionsForMap = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
    )
    public val requiredPermissionsForCall = arrayOf(
        Manifest.permission.CALL_PHONE,
    )
    public val requiredPermissionsForContacts = arrayOf(
        Manifest.permission.WRITE_CONTACTS,
        Manifest.permission.READ_CONTACTS
    )

    public fun isAllPermissionsGranted(context: Context, permissions: Array<String>): Boolean {
        for(p in permissions){
            if(ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_DENIED){
                return false
            }
        }

        return true
    }

    public fun requestDeniedPermissions(context: Context, requiredPermissions: Array<String>){
        val deniedPermissions = getDeniedPermissions(context, requiredPermissions)
        requestPermissions(context as Activity, deniedPermissions)
    }

    private fun getDeniedPermissions(context: Context, requiredPermissions: Array<String>): ArrayList<String> {
        var deniedPermissions = ArrayList<String>()

        for(p in requiredPermissions){
            if(ContextCompat.checkSelfPermission(context, p) == PackageManager.PERMISSION_DENIED){
                deniedPermissions.add(p)
            }
        }

        return deniedPermissions
    }

    private fun requestPermissions(activity: Activity, permissions: ArrayList<String>){
        if(permissions.isNotEmpty()){
            val array = arrayOfNulls<String>(permissions.size)
            ActivityCompat.requestPermissions(activity, permissions.toArray(array), REQUEST_CODE)
        }
    }
}