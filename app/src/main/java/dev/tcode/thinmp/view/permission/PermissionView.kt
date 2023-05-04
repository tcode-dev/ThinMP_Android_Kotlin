package dev.tcode.thinmp.view.permission

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant

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
                        Text(
                            stringResource(R.string.permission_denied),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = StyleConstant.PADDING_LARGE.dp, end = StyleConstant.PADDING_LARGE.dp, bottom = StyleConstant.PADDING_LARGE.dp)
                        )
                        Button(colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.primary, containerColor = MaterialTheme.colorScheme.onSecondary),
                            onClick = { permissionState.launchPermissionRequest() }) {
                            Text(stringResource(R.string.permission_request))
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