package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.view.collapsingTopAppBar.EditCollapsingTopAppBarView
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.row.EditRowView
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.view.swipe.SwipeToDismissView
import dev.tcode.thinmp.view.title.SectionTitleView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.OnEvent
import dev.tcode.thinmp.viewModel.MainEditViewModel

@Composable
fun MainEditScreen(viewModel: MainEditViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val navigator = LocalNavigator.current
    val callback = { viewModel.save() }

    OnEvent(viewModel.saved) { navigator.back() }
    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        EditCollapsingTopAppBarView(uiState.loaded, callback) {
            items(uiState.menu) { item ->
                EditRowView(stringResource(item.id), item.visibility, Modifier.clickable { viewModel.setMainMenuVisibility(item.key) })
            }
            // The menu rows above and the shortcuts below are lists, so they simply are not there
            // until the load fills them. These three are written out, so without the guard they
            // are on screen from the first frame: the two checkboxes showing their default of on
            // whatever is actually stored, and taking taps that load() then overwrites when it
            // lands. The section title is here for the same reason - a heading over nothing.
            if (uiState.loaded) {
                item {
                    EditRowView(stringResource(R.string.shortcut), uiState.shortcutVisibility, Modifier.clickable { viewModel.setShortcutVisibility() })
                }
                item {
                    EditRowView(stringResource(R.string.recently_added), uiState.recentlyAlbumsVisibility, Modifier.clickable { viewModel.setRecentlyAlbumsVisibility() })
                }
                item {
                    SectionTitleView(stringResource(R.string.shortcut))
                }
            }
            itemsIndexed(uiState.shortcuts) { index, shortcut ->
                SwipeToDismissView(shortcut.id.id, callback = { viewModel.removeShortcut(index) }) {
                    PlainRowView(shortcut.primaryText)
                }
            }
        }
    }
}