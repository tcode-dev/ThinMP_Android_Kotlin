package dev.tcode.thinmp.view.button

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import dev.tcode.thinmp.R
import dev.tcode.thinmp.constant.StyleConstant
import dev.tcode.thinmp.view.nav.LocalNavigator

@Composable
fun BackButtonView(modifier: Modifier = Modifier) {
    val navigator = LocalNavigator.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(StyleConstant.BUTTON_SIZE.dp)
            .clip(RoundedCornerShape(StyleConstant.IMAGE_CORNER_SIZE.dp))
            .clickable { navigator.back() }) {
        Icon(
            painter = painterResource(id = R.drawable.round_arrow_back_ios_24),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(StyleConstant.ICON_SIZE.dp)
        )
    }
}