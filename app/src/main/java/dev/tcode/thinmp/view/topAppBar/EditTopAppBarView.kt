package dev.tcode.thinmp.view.topAppBar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.nav.LocalNavigator
import dev.tcode.thinmp.view.title.PrimaryTitleView

/** Material's disabled content alpha, the same one the player's inactive icons use. */
private const val DISABLED_ALPHA = 0.38F

/**
 * [visible] fades the bar's background in as the list scrolls under it; the row of buttons is
 * outside that and always on screen. [enabled] is the done button on its own: the list arrives
 * asynchronously and saving before it does would write an empty list back over the stored one, so
 * the button stays inert - and looks it - until the screen has something real to save.
 */
@Composable
fun EditTopAppBarView(visible: Boolean, enabled: Boolean, callback: () -> Unit) {
    val navigator = LocalNavigator.current

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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(StyleConstant.ROW_HEIGHT.dp)
                .padding(start = StyleConstant.PADDING_LARGE.dp, end = StyleConstant.PADDING_LARGE.dp)
                .clickable {},
        ) {
            Box(contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(alignment = Alignment.CenterStart)
                    .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                    .clickable { navigator.back() }) {
                Text(stringResource(R.string.cancel), color = MaterialTheme.colorScheme.primary)
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier.align(alignment = Alignment.Center)) {
                PrimaryTitleView(stringResource(R.string.edit))
            }
            Box(contentAlignment = Alignment.Center, modifier = Modifier
                .align(alignment = Alignment.CenterEnd)
                .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
                .clickable(enabled = enabled) {
                    callback()
                }) {
                Text(
                    stringResource(R.string.done),
                    color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primary.copy(alpha = DISABLED_ALPHA)
                )
            }
        }
    }
}