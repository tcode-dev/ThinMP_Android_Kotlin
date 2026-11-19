package dev.tcode.thinmp.constant

/**
 * ショートカットの種類で、shortcuts.typeに名前で保存する。
 * 名前を変えると既存の行が読めなくなるので、変えるときはMigrationで列の値も書き換える。
 */
enum class ItemType {
    ARTIST, ALBUM, PLAYLIST
}
