package dev.tcode.thinmp.viewModel

/**
 * The playback position as a fraction of the track, for a Slider drawn over 0f..1f.
 *
 * `duration` is MediaStore's DURATION column, which is 0 when the metadata could not be read, and
 * the division used to be written inline with nothing guarding that. Dividing by a zero Float does
 * not throw, it produces a value the Slider cannot use: 0 / 0 is NaN, Compose rounds the fraction
 * while it lays the thumb out, and `roundToInt` refuses a NaN with `Cannot round NaN value` - the
 * player screen went down with it. A moment later, once the position has moved off 0, the same
 * expression is +Infinity, which the Slider does clamp, so a track that had only just started was
 * drawn as a finished one. There is no position to point at within a track of unknown length, so
 * the thumb sits at the start.
 *
 * Clamped because the two numbers come from different places: the duration is the tag, the
 * position is the file ExoPlayer is really playing, so a wrong tag puts the position past the end.
 */
fun sliderPosition(positionMs: Long, durationMs: Long): Float {
    if (durationMs <= 0) return 0f

    return (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
}
