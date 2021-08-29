package dev.tcode.thinmp.activity

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import dev.tcode.thinmp.permission.Permission

abstract class BaseActivity : AppCompatActivity() {
    protected abstract fun init()
    protected fun initWithPermissionCheck() {
        val permission: Permission = Permission.createInstance(this)
        if (permission.hasSelfPermissions()) {
            init()
        } else {
            permission.requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // If request is cancelled, the result arrays are empty.
        if (requestCode == Permission.PERMISSION_EXTERNAL_STORAGE_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                recreate()
            }
        }
    }
}