package dev.tcode.thinmp.viewModel

/**
 * 再生位置を曲の長さに対する割合(0f..1f)で返す。
 * Sliderに渡す値。
 *
 * `duration`はMediaStoreのDURATION列で、メタデータを読めなかったファイルでは0になる。
 * Floatの0で割っても例外にはならず、位置が0ならNaN(Composeが`Cannot round NaN value`で落ちる)、
 * それ以外は+Infinity(Sliderが末尾に丸め、始まったばかりの曲が終わったように見える)になる。
 * 長さが分からない曲では位置を示せないので、先頭にする。
 *
 * 長さはタグ、位置は実際に再生しているファイルから来るので、
 * タグが間違っていると位置が長さを超えることがある。
 * そのため範囲内に収める。
 */
fun sliderPosition(positionMs: Long, durationMs: Long): Float {
    if (durationMs <= 0) return 0f

    return (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)
}
