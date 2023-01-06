package dev.tcode.thinmp.view.screen

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.tcode.thinmp.view.image.ImageView
import dev.tcode.thinmp.view.util.CustomLifecycleEventObserver
import dev.tcode.thinmp.viewModel.PlayerViewModel

@ExperimentalFoundationApi
@Composable
fun PlayerScreen(
    navController: NavController,
    viewModel: PlayerViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    CustomLifecycleEventObserver(viewModel)

    ConstraintLayout(Modifier.fillMaxSize()) {
        val imageSize: Dp = LocalConfiguration.current.screenWidthDp.dp / 2

        ConstraintLayout(
            Modifier
                .fillMaxWidth()
                .height(LocalConfiguration.current.screenWidthDp.dp)
        ) {
            val (primary, secondary, tertiary) = createRefs()

            ImageView(
                uri = uiState.imageUri,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(10.dp),
                painter = null
            )
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                ImageView(
                    uri = uiState.imageUri,
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier
                        .size(imageSize)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .constrainAs(primary) {
                        top.linkTo(parent.bottom, margin = (-200).dp)
                    }
                    .background(
                        brush = Brush.verticalGradient(
                            0.0f to MaterialTheme.colors.surface.copy(alpha = 0F),
                            1.0F to MaterialTheme.colors.surface,
                        )
                    ),
            ) {
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .constrainAs(secondary) {
                        top.linkTo(parent.bottom, margin = (-90).dp)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {

                Text(
                    uiState.primaryText,
                    fontWeight = FontWeight.Bold,
                )
            }
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(25.dp)
                    .constrainAs(tertiary) {
                        top.linkTo(parent.bottom, margin = (-55).dp)
                    },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Text(
                    uiState.secondaryText,
                )
            }
        }
    }
}
