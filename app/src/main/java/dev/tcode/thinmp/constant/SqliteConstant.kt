package dev.tcode.thinmp.constant

/**
 * SQLite rejects a statement carrying more host parameters than SQLITE_MAX_VARIABLE_NUMBER, and
 * every `IN (:ids)` query here expands into one parameter per element - Room does it in the
 * generated binder, MediaStoreRepository does it by hand. So it is the length of the id list, not
 * the size of the table, that decides whether the query compiles at all, and that list is the
 * user's own data: their favourites, their playlist, their shortcuts.
 *
 * 999 was SQLite's limit for years; 3.32 raised the default to 32766, which is what the build on
 * API 36 reports. The app cannot rely on either number. The MediaStore queries are compiled by
 * MediaProvider, a separate process shipping its own SQLite, and the Room ones by whichever build
 * the device has. So the chunks are cut at the smaller, portable value, which is correct under
 * both - and a list long enough to need splitting belongs to a screen that is already reading a
 * whole library.
 *
 * Queries that bind arguments besides the list subtract them - see PlaylistSongDao.
 */
class SqliteConstant {
    companion object {
        const val MAX_VARIABLES = 999
    }
}
