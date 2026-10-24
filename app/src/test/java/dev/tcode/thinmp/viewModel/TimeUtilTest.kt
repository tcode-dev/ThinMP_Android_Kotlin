package dev.tcode.thinmp.viewModel

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

/**
 * タイムゾーンとロケールを変えても、ミリ秒が時刻ではなく長さとして扱われることを確かめる。
 * 結果は端末のタイムゾーンに左右されず、分が1時間ごとに0に戻ってもいけない。
 */
class TimeUtilTest {
    private val defaultTimeZone: TimeZone = TimeZone.getDefault()
    private val defaultLocale: Locale = Locale.getDefault()

    @Before
    fun setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tokyo"))
        Locale.setDefault(Locale.JAPAN)
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(defaultTimeZone)
        Locale.setDefault(defaultLocale)
    }

    @Test
    fun formatsMinutesAndSeconds() {
        assertEquals("00:00", formatTime(0))
        assertEquals("00:09", formatTime(9_000))
        assertEquals("03:25", formatTime(205_000))
    }

    @Test
    fun truncatesPartialSeconds() {
        assertEquals("00:01", formatTime(1_999))
    }

    /** オフセットが1時間単位でないゾーンでも、30分ずれないこと。 */
    @Test
    fun isTheSameInEveryTimeZone() {
        val zones = listOf("UTC", "Asia/Tokyo", "Asia/Kolkata", "Asia/Kathmandu", "Australia/Adelaide", "America/New_York")

        zones.forEach { zone ->
            TimeZone.setDefault(TimeZone.getTimeZone(zone))

            assertEquals(zone, "03:00", formatTime(180_000))
        }
    }

    /** 時刻の分ではなく経過した分なので、1時間の曲が00:00にならないこと。 */
    @Test
    fun keepsCountingPastAnHour() {
        assertEquals("60:00", formatTime(3_600_000))
        assertEquals("75:30", formatTime(4_530_000))
    }

    /** 独自の数字を使うロケールでもASCIIの数字になること。 */
    @Test
    fun formatsInAsciiDigits() {
        Locale.setDefault(Locale.forLanguageTag("ar-EG-u-nu-arab"))

        assertEquals("03:25", formatTime(205_000))
    }

    @Test
    fun treatsAnUnknownDurationAsZero() {
        assertEquals("00:00", formatTime(-1))
    }
}
