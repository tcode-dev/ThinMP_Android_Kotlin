package dev.tcode.thinmp.viewModel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

/**
 * 重要なのは長さが分からないケース。
 * SliderにNaNを渡すとプレーヤー画面ごと落ちるので、値が正しいかと同じくらい、
 * 数値であることが重要になる。
 */
class SliderUtilTest {
    private val delta = 0.0001f

    @Test
    fun isThePositionAsAFractionOfTheTrack() {
        assertEquals(0f, sliderPosition(0, 200_000), delta)
        assertEquals(0.5f, sliderPosition(100_000, 200_000), delta)
        assertEquals(1f, sliderPosition(200_000, 200_000), delta)
    }

    /** メタデータを読めなかったファイルの長さは、MediaStoreでは0になる。 */
    @Test
    fun treatsAnUnknownDurationAsTheStartOfTheTrack() {
        assertFalse("a NaN reaches the Slider and crashes the layout", sliderPosition(0, 0).isNaN())
        assertFalse("a NaN reaches the Slider and crashes the layout", sliderPosition(5_000, 0).isNaN())

        assertEquals(0f, sliderPosition(0, 0), delta)
        assertEquals(0f, sliderPosition(5_000, 0), delta)
    }

    /** DURATIONはInt列なので、負の値も0と同じく壊れたタグとして扱う。 */
    @Test
    fun treatsANegativeDurationAsTheStartOfTheTrack() {
        assertFalse("an infinity is as unroundable as a NaN", sliderPosition(5_000, -1).isInfinite())

        assertEquals(0f, sliderPosition(5_000, -1), delta)
    }

    /** 長さはタグ、位置はファイルから来るので、位置が長さを超えることがある。 */
    @Test
    fun staysWithinTheSlidersRange() {
        assertEquals(1f, sliderPosition(300_000, 200_000), delta)
        assertEquals(0f, sliderPosition(-1, 200_000), delta)
    }
}
