package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.row.EditRowView
import dev.tcode.thinmp.view.topAppBar.EditTopAppBarView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.EmptyTopAppBarView
import dev.tcode.thinmp.viewModel.MainEditViewModel

@ExperimentalFoundationApi
@Composable
fun MainEditScreen(viewModel: MainEditViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val lazyListState = rememberLazyListState()
    val navigator = LocalNavigator.current

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        Box(Modifier.zIndex(3F)) {
            EditTopAppBarView(stringResource(R.string.edit), lazyListState.firstVisibleItemScrollOffset) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                    .clickable {
                        viewModel.save(context)
                        navigator.back()
                    }) {
                    Text(stringResource(R.string.done), color = MaterialTheme.colorScheme.primary)
                }
            }
        }
        LazyColumn(state = lazyListState) {
            item {
                EmptyTopAppBarView()
            }
            items(uiState.menu) { item ->
                EditRowView(stringResource(item.id), item.visibility, Modifier.clickable { viewModel.setMainMenuVisibility(item.key) })
            }
            item {
                EditRowView(stringResource(R.string.shortcut), uiState.shortcutVisibility, Modifier.clickable { viewModel.setShortcutVisibility() })
            }
            item {
                EditRowView(stringResource(R.string.recently_added), uiState.recentlyAlbumsVisibility, Modifier.clickable { viewModel.setRecentlyAlbumsVisibility() })
            }
        }
    }
}