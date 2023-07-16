package dev.tcode.thinmp.view.topAppBar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.button.BackButtonView
import dev.tcode.thinmp.view.title.PrimaryTitleView

@Composable
fun PlainTopAppBarView(title: String, visible: Boolean) {
    Box {
        AnimatedVisibility(
            visible = visible, enter = fadeIn(), exit = fadeOut()
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
                .padding(start = StyleConstant.PADDING_TINY.dp, end = StyleConstant.BUTTON_SIZE.dp + StyleConstant.PADDING_TINY.dp)
                .clickable {}, verticalAlignment = Alignment.CenterVertically
        ) {
            BackButtonView()
            PrimaryTitleView(title)
        }
    }
}