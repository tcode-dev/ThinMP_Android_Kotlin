package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.row.MediaRowView
import dev.tcode.thinmp.view.topAppBar.EditTopAppBarView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.EmptyTopbarView
import dev.tcode.thinmp.viewModel.FavoriteSongsViewModel

@ExperimentalFoundationApi
@Composable
fun FavoriteSongsEditScreen(viewModel: FavoriteSongsViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
//    val dragDropListState = rememberLazyListState(onMove = onMove)
    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        Box(Modifier.zIndex(3F)) {
            EditTopAppBarView(stringResource(R.string.edit), lazyListState.firstVisibleItemScrollOffset) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                    .clickable { }) {
                    Text(stringResource(R.string.done))
                }
            }
        }
        LazyColumn(
            modifier = Modifier.pointerInput(Unit) {
                detectDragGesturesAfterLongPress(onDrag = { change, offset ->
                    change.consume()
//                                dragDropListState.onDrag(offset)

//                                if (overscrollJob?.isActive == true)
//                                    return@detectDragGesturesAfterLongPress

//                                dragDropListState.checkForOverScroll()
//                                    .takeIf { it != 0f }
//                                    ?.let { overscrollJob = scope.launch { dragDropListState.lazyListState.scrollBy(it) } }
//                                    ?: run { overscrollJob?.cancel() }
                }, onDragStart = { offset -> }, onDragEnd = { }, onDragCancel = { })
            }, state = lazyListState
        ) {
            item {
                EmptyTopbarView()
            }
            itemsIndexed(uiState.songs) { index, song ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.TopStart)
                ) {
                    MediaRowView(song.name, song.artistName, song.getImageUri())
                }
            }
        }
    }
}