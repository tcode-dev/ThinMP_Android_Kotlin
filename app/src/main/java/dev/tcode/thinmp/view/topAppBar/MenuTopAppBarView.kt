package dev.tcode.thinmp.view.topAppBar

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.button.BackButtonView
import dev.tcode.thinmp.view.title.PrimaryTitleView

@Composable
fun MenuTopAppBarView(title: String, offset: Int, content: @Composable BoxScope.() -> Unit) {
    Box {
        AnimatedVisibility(
            visible = offset > 1, enter = fadeIn(initialAlpha = 0.3F), exit = fadeOut(targetAlpha = 0.3F)
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
                .clickable {},
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BackButtonView()
            PrimaryTitleView(title)
            Box(content = content)
        }
    }
}