package dev.tcode.thinmp.view.permission

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PermissionView(content: @Composable BoxScope.() -> Unit) {
    val permissionState = rememberPermissionState(
        android.Manifest.permission.READ_MEDIA_AUDIO
    )

    when (permissionState.status) {
        PermissionStatus.Granted -> {
            Box(content = content)
        }
        is PermissionStatus.Denied -> {
            when {
                permissionState.status.shouldShowRationale -> {
                    Column(
                        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Denied")
                        Button(onClick = { permissionState.launchPermissionRequest() }) {
                            Text("Request permission")
                        }
                    }
                }
                else -> SideEffect {
                    permissionState.launchPermissionRequest()
                }
            }
        }
    }
}