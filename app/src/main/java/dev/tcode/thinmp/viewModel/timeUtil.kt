package dev.tcode.thinmp.viewModel

import java.util.Locale

/**
 * Formats a duration in milliseconds as mm:ss.
 *
 * This used to be `String.format("%1\$tM:%1\$tS", ms)`, which reads the Long as an instant since
 * the epoch and renders it in the default time zone. `%tM` is the minute within its hour, not the
 * elapsed minutes, so anything past an hour wrapped - and in a zone whose offset is not a whole
 * hour (Asia/Kolkata, Asia/Kathmandu, Australia/Adelaide, ...) every single time was off by that
 * offset: a three minute song read 33:00. JST is a whole hour ahead, which is why it looked right
 * here.
 *
 * Minutes are not capped at 60, so an hour long track reads 60:00 rather than starting over.
 * Locale.US keeps the digits ASCII in locales that would otherwise render them in their own
 * numerals.
 */
fun formatTime(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0) / 1000

    return String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}
