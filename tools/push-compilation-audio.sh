#!/bin/sh
# 接続中の端末かエミュレーターに、2人のアーティストによるコンピレーションを入れる。
# ShortcutServiceTestが確かめている状態を、手で再現するためのもの。
#
# アルバムのテーブルはアルバムごとにartist_idを1つしか持たないので、
# 2人のうち1人はアルバムを持たないアーティストになる。
# それでもアーティスト一覧に出てショートカットにできるので、
# そのショートカットでメイン画面が表示できることを確かめる。
#
# 音声データは入れない。
# 生成したWAVにはタグを入れられないので(generate-test-audio.pyを参照)、
# artistとalbumはMediaStoreの列に直接書き込む。
# 再生はせず、一覧に出すためだけに使う。
#
#   tools/push-compilation-audio.sh          2曲を追加する
#   tools/push-compilation-audio.sh remove   追加した2曲を削除する
set -eu

album=thinmp_test_compilation
artist_prefix=thinmp_test_artist

delete() {
    adb shell content delete --uri content://media/external/audio/media --where "\"album='$album'\""
}

insert() {
    adb shell content insert --uri content://media/external/audio/media \
        --bind relative_path:s:Music/ \
        --bind _display_name:s:"$1.mp3" \
        --bind title:s:"$1" \
        --bind artist:s:"$2" \
        --bind album:s:"$album" \
        --bind mime_type:s:audio/mpeg \
        --bind is_music:i:1
}

query() {
    adb shell content query --uri "$1" --projection "$2" --where "\"$3\"" | tr -d '\r'
}

if [ "${1:-}" = remove ]; then
    delete
    echo "removed"
    exit 0
fi

# 2回実行しても4行にならないよう、挿入の前に削除する。
delete
insert "${album}_a" "${artist_prefix}_a"
insert "${album}_b" "${artist_prefix}_b"

echo "artists:"
query content://media/external/audio/artists _id:artist "artist LIKE '$artist_prefix%'"
echo "album:"
query content://media/external/audio/albums _id:album:artist_id "album='$album'"

# どちらがアルバムのアーティストになるかはMediaStoreが決めるので、問い合わせて確かめる。
# もう1人が、ショートカットにするアーティスト。
album_artist=$(query content://media/external/audio/albums artist_id "album='$album'" | sed -n 's/.*artist_id=\([0-9]*\).*/\1/p')
orphan=$(query content://media/external/audio/artists _id:artist "artist LIKE '$artist_prefix%'" | grep -v "_id=$album_artist," | sed -n 's/.*artist=\(.*\)$/\1/p')

echo
echo "shortcut this one to reproduce the crash: $orphan"
