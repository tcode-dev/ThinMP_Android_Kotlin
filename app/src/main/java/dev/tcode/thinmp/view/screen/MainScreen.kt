package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.view.util.DividerView
import dev.tcode.thinmp.view.util.EmptyMiniPlayerView
import dev.tcode.thinmp.viewModel.MainViewModel

@Composable
fun MainScreen(navController: NavController, viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        val (miniPlayer) = createRefs()
        val miniPlayerHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().value + StyleConstant.ROW_HEIGHT

        Column {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(start = StyleConstant.PADDING_LARGE.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = StyleConstant.PADDING_MEDIUM.dp)
                        .height(StyleConstant.ROW_HEIGHT.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "Library", textAlign = TextAlign.Left, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold, fontSize = 30.sp
                    )
                    Box(contentAlignment = Alignment.Center, modifier = Modifier
                        .size(StyleConstant.BUTTON_SIZE.dp)
                        .clickable { }) {
                        Icon(painter = painterResource(id = R.drawable.round_more_vert_24), contentDescription = null, modifier = Modifier.size(StyleConstant.ICON_SIZE.dp))
                    }
                }
                DividerView()
            }
            uiState.menuList.forEach { menu ->
                PlainRowView(menu.label, modifier = Modifier.clickable {
                    navController.navigate(menu.key)
                })
            }
            EmptyMiniPlayerView()
        }
        Box(modifier = Modifier.constrainAs(miniPlayer) {
            top.linkTo(parent.bottom, margin = (-miniPlayerHeight).dp)
        }) {
            MiniPlayerView(navController)
        }
    }
}
