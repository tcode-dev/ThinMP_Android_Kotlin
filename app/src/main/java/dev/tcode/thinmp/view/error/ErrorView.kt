package dev.tcode.thinmp.view.error

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.viewModel.ErrorViewModel

/**
 * MediaStoreの問い合わせが失敗している間は、[content]の代わりにエラーと再読み込みのボタンを出す。
 * 再読み込みすると[content]を作り直すので、NavControllerも作り直され、メイン画面からすべての画面が読み込み直す。
 * 編集画面は最初に1回しか読み込まないので、元の画面に戻さずにメイン画面から始め直す。
 */
@Composable
fun ErrorView(viewModel: ErrorViewModel = hiltViewModel(), content: @Composable BoxScope.() -> Unit) {
    val failed by viewModel.failed.collectAsState()

    if (failed) {
        ErrorDescriptionView {
            viewModel.reload()
        }
    } else {
        CompositionLocalProvider(LocalViewModelStoreOwner provides viewModel.contentOwner) {
            Box(content = content)
        }
    }
}

@Composable
fun ErrorDescriptionView(callback: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            stringResource(R.string.media_store_query_failed),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = StyleConstant.PADDING_LARGE.dp, end = StyleConstant.PADDING_LARGE.dp, bottom = StyleConstant.PADDING_LARGE.dp)
        )
        Button(colors = ButtonDefaults.buttonColors(contentColor = MaterialTheme.colorScheme.primary, containerColor = MaterialTheme.colorScheme.onSecondary), onClick = { callback() }) {
            Text(stringResource(R.string.reload))
        }
    }
}
