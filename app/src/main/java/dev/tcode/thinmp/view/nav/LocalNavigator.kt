package dev.tcode.thinmp.view.nav

import androidx.compose.runtime.staticCompositionLocalOf

val LocalNavigator = staticCompositionLocalOf<Navigator> {
    error("Navigator not found")
}