package dev.tcode.thinmp.permission

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class Permission private constructor(private val context: Context) {
    fun hasSelfPermissions(): Boolean {
        val permission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        return permission == PackageManager.PERMISSION_GRANTED
    }

    fun requestPermissions() {
        ActivityCompat.requestPermissions(
            (context as Activity), arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
            PERMISSION_EXTERNAL_STORAGE_CODE
        )
    }

    companion object {
        const val PERMISSION_EXTERNAL_STORAGE_CODE = 1
        fun createInstance(context: Context): Permission {
            return Permission(context)
        }
    }
}