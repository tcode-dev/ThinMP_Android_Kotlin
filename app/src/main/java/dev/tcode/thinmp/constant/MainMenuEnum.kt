package dev.tcode.thinmp.constant

import dev.tcode.thinmp.activity.SongsActivity

enum class MainMenuEnum(val key: String, val label: String, val link: Class<*>) {
    SONGS("songs", "Songs", SongsActivity::class.java)
}