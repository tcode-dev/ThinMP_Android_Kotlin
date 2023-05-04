package dev.tcode.thinmp.view.topAppBar

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.button.BackButtonView
import dev.tcode.thinmp.view.title.PrimaryTitleView

@Composable
fun DetailTopAppBarView(title: String, visible: Boolean, toggle: () -> Unit) {
    Box {
        AnimatedVisibility(
            visible = visible, enter = fadeIn(initialAlpha = 0.3F), exit = fadeOut(targetAlpha = 0.3F)
        ) {
            Box(
                modifier = Modifier
                    .background(color = MaterialTheme.colorScheme.onBackground)
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .height(StyleConstant.ROW_HEIGHT.dp)
            ) {}
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(StyleConstant.ROW_HEIGHT.dp)
                .padding(start = StyleConstant.PADDING_TINY.dp, end = StyleConstant.PADDING_TINY.dp)
                // クリック時に下の要素が反応しないように空のclickableを設定
                .clickable {}, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BackButtonView()
            if (visible) {
                PrimaryTitleView(title)
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .size(StyleConstant.BUTTON_SIZE.dp)
                .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                .clickable { toggle() }) {
                Icon(
                    painter = painterResource(id = R.drawable.round_more_vert_24),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(StyleConstant.ICON_SIZE.dp)
                )
            }
        }
    }
}