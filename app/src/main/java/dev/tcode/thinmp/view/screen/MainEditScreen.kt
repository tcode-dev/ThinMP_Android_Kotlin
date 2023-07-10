package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.view.collapsingTopAppBar.EditCollapsingTopAppBarView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.row.EditRowView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.viewModel.MainEditViewModel

@ExperimentalFoundationApi
@Composable
fun MainEditScreen(viewModel: MainEditViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val navigator = LocalNavigator.current
    val doneCallback = {
        viewModel.save(context)
        navigator.back()
    }

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        EditCollapsingTopAppBarView(doneCallback) {
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