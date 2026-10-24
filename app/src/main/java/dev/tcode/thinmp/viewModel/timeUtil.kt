package dev.tcode.thinmp.viewModel

import java.util.Locale

/**
 * ミリ秒をmm:ssに整形する。
 *
 * `%tM:%tS`を使わず秒から計算する。
 * `%tM:%tS`は値をエポックからの時刻として既定のタイムゾーンで読むので、1時間を超えると分が0に戻り、
 * オフセットが1時間単位でないゾーン(Asia/Kolkataなど)ではそのぶんずれる。
 *
 * 分は60で折り返さないので、1時間の曲は60:00になる。
 * Locale.USを指定して、独自の数字を使うロケールでもASCIIの数字で表示する。
 */
fun formatTime(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0) / 1000

    return String.format(Locale.US, "%02d:%02d", totalSeconds / 60, totalSeconds % 60)
}
