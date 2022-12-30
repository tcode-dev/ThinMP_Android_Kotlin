package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import dev.tcode.thinmp.view.player.MiniPlayerView
import dev.tcode.thinmp.view.row.PlainRowView
import dev.tcode.thinmp.viewModel.MainViewModel

@Composable
fun MainScreen(navController: NavController, viewModel: MainViewModel = MainViewModel()) {
    val uiState = viewModel.uiState

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(50.dp)
                .padding(start = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                "Library",
                textAlign = TextAlign.Left,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .width(50.dp)
                    .height(50.dp)
                    .clickable { }) {
            }
        }
        uiState.menuList.forEach { menu ->
            PlainRowView(menu.label, modifier = Modifier
                .clickable {
                    navController.navigate(menu.key)
                })
        }
        MiniPlayerView()
    }
}
