#!/bin/sh
# Puts the test tones from tools/testdata on a connected device or emulator.
#
# The app lists what MediaStore knows about, so a device with no audio shows an
# empty screen and DuplicateEntryServiceTest skips itself.
set -eu

directory=$(dirname "$0")

if [ ! -f "$directory/testdata/thinmp_test_a.wav" ]; then
    python3 "$directory/generate-test-audio.py"
fi

for file in "$directory"/testdata/*.wav; do
    adb push "$file" /sdcard/Music/
done

# Pushing into /sdcard/Music is enough on its own - MediaProvider indexes the
# write as it happens - but a device that somehow missed it can be told to look
# again, and asking twice costs nothing.
adb shell 'content call --uri content://media --method scan_volume --arg external_primary' > /dev/null

adb shell content query --uri content://media/external/audio/media --projection _id:title
