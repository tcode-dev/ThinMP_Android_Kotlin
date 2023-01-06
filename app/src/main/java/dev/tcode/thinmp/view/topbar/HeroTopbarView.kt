package dev.tcode.thinmp.view.topbar

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant

@Composable
fun HeroTopbarView(navController: NavController, title: String, visible: Boolean) {
    Box {
        AnimatedVisibility(
            visible = visible, enter = fadeIn(initialAlpha = 0.3F), exit = fadeOut(targetAlpha = 0.3F)
        ) {
            Box(
                modifier = Modifier
                    .background(color = MaterialTheme.colors.secondary)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(StyleConstant.ROW_HEIGHT.dp)
            ) {}
        }
        if (visible) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(StyleConstant.ROW_HEIGHT.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(StyleConstant.BUTTON_SIZE.dp)
                    .clickable { navController.popBackStack() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.round_arrow_back_ios_24), contentDescription = null, modifier = Modifier.size(StyleConstant.ICON_SIZE.dp)
                    )
                }
                Text(
                    title,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                )
                Box(contentAlignment = Alignment.Center, modifier = Modifier
                    .size(StyleConstant.BUTTON_SIZE.dp)
                    .clickable { }) {}
            }
        }
    }
}
