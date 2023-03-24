package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.topAppBar.EditTopAppBarView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.DividerView
import dev.tcode.thinmp.view.util.EmptyTopbarView
import dev.tcode.thinmp.viewModel.MainViewModel

@ExperimentalFoundationApi
@Composable
fun MainEditScreen(
    navController: NavController, viewModel: MainViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val lazyListState = rememberLazyListState()
    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        Box(Modifier.zIndex(3F)) {
            EditTopAppBarView(navController, stringResource(R.string.edit), lazyListState.firstVisibleItemScrollOffset) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                    .clickable { }) {
                    Text(stringResource(R.string.done))
                }
            }
        }
        LazyColumn(state = lazyListState) {
            item {
                EmptyTopbarView()
            }
            items(uiState.menu) { item ->
                var checked by remember { mutableStateOf(false) }
                val onCheckedChange = { checked = !checked }

                Column(modifier = Modifier
                    .height(StyleConstant.ROW_HEIGHT.dp)
                    .padding(start = StyleConstant.PADDING_LARGE.dp)
                    .clickable { onCheckedChange() }) {
                    Row(
                        modifier = Modifier.height(StyleConstant.ROW_HEIGHT.dp), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(
                                id = if (checked) {
                                    R.drawable.check_box
                                } else {
                                    R.drawable.check_box_outline_blank
                                }
                            ), contentDescription = null, modifier = Modifier.size(88.dp)
                        )
                        Text(stringResource(item.id), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                    DividerView()
                }
            }
        }
    }
}