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
import dev.tcode.thinmp.view.util.OnEvent
import dev.tcode.thinmp.viewModel.MainEditViewModel

@Composable
fun MainEditScreen(viewModel: MainEditViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val navigator = LocalNavigator.current
    val callback = { viewModel.save() }

    OnEvent(viewModel.saved) { navigator.back() }

    ConstraintLayout(Modifier.fillMaxSize()) {
        EditCollapsingTopAppBarView(uiState.loaded, callback) {
            items(uiState.menu, key = { it.key }) { item ->
                EditRowView(stringResource(item.id), item.visibility, Modifier.clickable { viewModel.setMainMenuVisibility(item.key) })
            }
            // メニューとショートカットは一覧なので読み込むまで表示されないが、
            // この3行は固定なので最初から表示されてしまう。
            // 読み込み前のチェックボックスは保存値ではなく既定のオンを表示し、
            // タップしてもload()で上書きされる。
            // 見出しだけが出るのも避けるため、まとめて読み込み後に表示する。
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
            itemsIndexed(uiState.shortcuts, key = { _, shortcut -> shortcut.id.id }) { index, shortcut ->
                SwipeToDismissView(callback = { viewModel.removeShortcut(index) }) {
                    PlainRowView(shortcut.primaryText)
                }
            }
        }
    }
}