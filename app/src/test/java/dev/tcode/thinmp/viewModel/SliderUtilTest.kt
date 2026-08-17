package dev.tcode.thinmp.viewModel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * The unknown duration cases are the regression guards: a Slider handed NaN takes the player
 * screen down while it lays the thumb out, so what matters about them is as much that the answer
 * is a number at all as which number it is.
 */
class SliderUtilTest {
    private val delta = 0.0001f

    @Test
    fun isThePositionAsAFractionOfTheTrack() {
        assertEquals(0f, sliderPosition(0, 200_000), delta)
        assertEquals(0.5f, sliderPosition(100_000, 200_000), delta)
        assertEquals(1f, sliderPosition(200_000, 200_000), delta)
    }

    /** MediaStore reports 0 for a file whose metadata could not be read. */
    @Test
    fun treatsAnUnknownDurationAsTheStartOfTheTrack() {
        assertFalse("a NaN reaches the Slider and crashes the layout", sliderPosition(0, 0).isNaN())
        assertFalse("a NaN reaches the Slider and crashes the layout", sliderPosition(5_000, 0).isNaN())

        assertEquals(0f, sliderPosition(0, 0), delta)
        assertEquals(0f, sliderPosition(5_000, 0), delta)
    }

    /** DURATION is an Int column, so a negative is as much a broken tag as a 0. */
    @Test
    fun treatsANegativeDurationAsTheStartOfTheTrack() {
        assertFalse("an infinity is as unroundable as a NaN", sliderPosition(5_000, -1).isInfinite())

        assertEquals(0f, sliderPosition(5_000, -1), delta)
    }

    /** The duration is the tag; the position is the file, and it can run past what the tag claims. */
    @Test
    fun staysWithinTheSlidersRange() {
        assertEquals(1f, sliderPosition(300_000, 200_000), delta)
        assertEquals(0f, sliderPosition(-1, 200_000), delta)
    }
}
