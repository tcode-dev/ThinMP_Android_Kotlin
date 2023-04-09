package dev.tcode.thinmp.view.topAppBar

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Surface
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant

@Composable
fun PlainTopAppBarView(navController: NavController, title: String, offset: Int) {
    Box {
        AnimatedVisibility(
            visible = offset > 1, enter = fadeIn(), exit = fadeOut()
        ) {
            Surface(
                color = MaterialTheme.colorScheme.onBackground, modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(StyleConstant.ROW_HEIGHT.dp)
                ) {}
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(StyleConstant.ROW_HEIGHT.dp)
                .padding(start = StyleConstant.PADDING_TINY.dp, end = StyleConstant.BUTTON_SIZE.dp + StyleConstant.PADDING_TINY.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .size(StyleConstant.BUTTON_SIZE.dp)
                .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                .clickable { navController.popBackStack() }) {
                Icon(painter = painterResource(id = R.drawable.round_arrow_back_ios_24), contentDescription = null, modifier = Modifier.size(StyleConstant.ICON_SIZE.dp))
            }
            Text(
                title,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(LocalConfiguration.current.screenWidthDp.dp - StyleConstant.BUTTON_SIZE.dp * 2 - StyleConstant.PADDING_TINY.dp * 2)
            )
        }
    }
}