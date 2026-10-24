#!/bin/sh
# tools/testdataのテスト用の音声を、接続中の端末かエミュレーターに入れる。
#
# アプリはMediaStoreにある曲を表示するので、音声がない端末では画面が空になり、
# DuplicateEntryServiceTestはスキップされる。
set -eu

directory=$(dirname "$0")

if [ ! -f "$directory/testdata/thinmp_test_a.wav" ]; then
    python3 "$directory/generate-test-audio.py"
fi

for file in "$directory"/testdata/*.wav; do
    adb push "$file" /sdcard/Music/
done

# /sdcard/Musicにpushすれば通常はMediaProviderがすぐにインデックスするが、念のため再スキャンする。
adb shell 'content call --uri content://media --method scan_volume --arg external_primary' > /dev/null

adb shell content query --uri content://media/external/audio/media --projection _id:title
