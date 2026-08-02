#!/bin/sh
# Puts a two artist compilation on a connected device or emulator, so that the
# crash ShortcutServiceTest covers can be walked through by hand.
#
# The albums collection holds one row per album carrying one artist_id, so of
# two artists sharing an album only one is the artist of an album. The other is
# still listed under Artists and can be shortcut like anyone else, and doing
# that used to take the main screen down: ShortcutService read its image from
# the first album that artist does not have.
#
# These rows carry no audio. artist and album are written as MediaStore columns
# rather than read from a tag block, because a generated WAV has no tag block to
# put them in - see generate-test-audio.py. Nothing plays them; they exist to be
# listed.
#
#   tools/push-compilation-audio.sh          add the two tracks
#   tools/push-compilation-audio.sh remove   take them away again
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

# Also runs before the inserts, so a second run does not end up with four rows.
delete
insert "${album}_a" "${artist_prefix}_a"
insert "${album}_b" "${artist_prefix}_b"

echo "artists:"
query content://media/external/audio/artists _id:artist "artist LIKE '$artist_prefix%'"
echo "album:"
query content://media/external/audio/albums _id:album:artist_id "album='$album'"

# Which of the two the album row names is MediaStore's choice, so ask rather
# than assume: the other one is the artist to shortcut.
album_artist=$(query content://media/external/audio/albums artist_id "album='$album'" | sed -n 's/.*artist_id=\([0-9]*\).*/\1/p')
orphan=$(query content://media/external/audio/artists _id:artist "artist LIKE '$artist_prefix%'" | grep -v "_id=$album_artist," | sed -n 's/.*artist=\(.*\)$/\1/p')

echo
echo "shortcut this one to reproduce the crash: $orphan"
