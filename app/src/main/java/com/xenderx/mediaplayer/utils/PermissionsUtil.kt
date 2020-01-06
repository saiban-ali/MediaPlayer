package com.xenderx.mediaplayer.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionsUtil: ActivityCompat.OnRequestPermissionsResultCallback {

    private val storagePermissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )
    private const val STORAGE_REQUEST_CODE = 1001

    private lateinit var activity: Activity

    fun checkStoragePermissions(activity: Activity): Boolean {

        PermissionsUtil.activity = activity

        if (ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity,
                storagePermissions,
                STORAGE_REQUEST_CODE
            )
        }

        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == STORAGE_REQUEST_CODE && grantResults.isNotEmpty()) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                checkStoragePermissions(
                    activity
                )
            }
        }
    }

}