package dev.tcode.thinmp.viewModel

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.Locale
import java.util.TimeZone

/**
 * The zone and locale cases are the regression guards. The old format read the millisecond count
 * as an instant since the epoch, so the answer depended on the device's time zone, and the minute
 * restarted every hour.
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

    /** A zone offset that is not a whole hour used to shift every time by 30 minutes. */
    @Test
    fun isTheSameInEveryTimeZone() {
        val zones = listOf("UTC", "Asia/Tokyo", "Asia/Kolkata", "Asia/Kathmandu", "Australia/Adelaide", "America/New_York")

        zones.forEach { zone ->
            TimeZone.setDefault(TimeZone.getTimeZone(zone))

            assertEquals(zone, "03:00", formatTime(180_000))
        }
    }

    /** %tM is the minute within its hour, so an hour long track used to read 00:00. */
    @Test
    fun keepsCountingPastAnHour() {
        assertEquals("60:00", formatTime(3_600_000))
        assertEquals("75:30", formatTime(4_530_000))
    }

    /** A locale with its own numerals must not reach the digits. */
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
