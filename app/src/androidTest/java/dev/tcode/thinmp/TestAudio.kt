package dev.tcode.thinmp

import org.junit.Assert.assertTrue

/**
 * 端末に音声ファイルがcount個以上あることを確かめ、なければ失敗させる。
 * スキップにすると、音声を入れ忘れたまま流しても、テストが通ったように見えるからだ。
 */
fun assertHasAudio(found: Int, count: Int = 1) {
    assertTrue("needs at least $count audio file(s) in MediaStore, found $found: run tools/push-test-audio.sh", found >= count)
}
