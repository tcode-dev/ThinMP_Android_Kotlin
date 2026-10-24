package dev.tcode.thinmp.viewModel

import java.util.Locale

/**
 * Formats a duration in milliseconds as mm:ss.
 *
 * Computed from the seconds rather than with `%tM:%tS`, which read the Long as an instant since the
 * epoch in the default time zone. `%tM` is the minute within its hour, not the elapsed minutes, so
 * anything past an hour would wrap - and in a zone whose offset is not a whole hour (Asia/Kolkata,
 * Asia/Kathmandu, Australia/Adelaide, ...) every time would be off by that offset.
 *
 * Minutes are not capped at 60, so an hour long track reads 60:00 rather than starting over.
 * Locale.US keeps the digits ASCII in locales that would otherwise render them in their own
 * numerals.
 */
fun formatTime(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0) / 1000

    return String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}
