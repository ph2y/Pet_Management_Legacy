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
    companion object{
        private const val REQUEST_CODE = 100

        val requiredPermissionsForLocation = arrayOf(
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
        val requiredPermissionsForCall = arrayOf(
            Manifest.permission.CALL_PHONE,
        )
        val requiredPermissionsForContacts = arrayOf(
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.READ_CONTACTS
        )

        fun isAllPermissionsGranted(context: Context, permissions: Array<String>): Boolean {
            for(p in permissions){
                if(ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED){
                    return false
                }
            }

            return true
        }

        fun requestNotGrantedPermissions(context: Context, requiredPermissions: Array<String>){
            val notGrantedPermissions = getNotGrantedPermissions(context, requiredPermissions)
            requestPermissions(context as Activity, notGrantedPermissions)
        }

        private fun getNotGrantedPermissions(context: Context, requiredPermissions: Array<String>): ArrayList<String> {
            var notGrantedPermissions = ArrayList<String>()

            for(p in requiredPermissions){
                if(ContextCompat.checkSelfPermission(context, p) != PackageManager.PERMISSION_GRANTED){
                    notGrantedPermissions.add(p)
                }
            }

            return notGrantedPermissions
        }

        private fun requestPermissions(activity: Activity, permissions: ArrayList<String>){
            if(permissions.isNotEmpty()){
                val array = arrayOfNulls<String>(permissions.size)
                ActivityCompat.requestPermissions(activity, permissions.toArray(array), REQUEST_CODE)
            }
        }
    }
}