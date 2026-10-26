package dev.tcode.thinmp.view.permission

import android.Manifest.permission.READ_MEDIA_AUDIO
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
    val initialized = remember { mutableStateOf(false) }
    val audio = rememberPermissionState(READ_MEDIA_AUDIO) {
        initialized.value = true
    }

    when {
        audio.status == PermissionStatus.Granted -> {
            Box(content = content)
        }

        audio.status.shouldShowRationale -> {
            DescriptionView {
                audio.launchPermissionRequest()
            }
        }

        else -> {
            LaunchedEffect(Unit) {
                audio.launchPermissionRequest()
            }
            if (initialized.value) {
                DescriptionView {
                    audio.launchPermissionRequest()
                }
            }
        }
    }
}

@Composable
fun DescriptionView(callback: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.permission_audio_denied),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = StyleConstant.PADDING_LARGE.dp, end = StyleConstant.PADDING_LARGE.dp, bottom = StyleConstant.PADDING_LARGE.dp)
        )
        Text(
            stringResource(R.string.permission_denied_advice),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = StyleConstant.PADDING_LARGE.dp, end = StyleConstant.PADDING_LARGE.dp, bottom = StyleConstant.PADDING_LARGE.dp)
        )
        Button(colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.primary, containerColor = MaterialTheme.colorScheme.onSecondary), onClick = { callback() }) {
            Text(stringResource(R.string.permission_request))
        }
    }
}