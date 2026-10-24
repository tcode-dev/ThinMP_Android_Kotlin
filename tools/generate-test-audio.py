#!/usr/bin/env python3
"""tools/push-test-audio.shが端末に入れるWAVファイルを生成する。

MediaStoreに曲を表示させるには実際の音声ファイルが必要だが、音楽ファイルはコミットできない
ので、2秒の正弦波を生成する。WAVにはタグがないので、タイトルはファイル名になる。
"""
import math
import struct
import wave
from pathlib import Path

SAMPLE_RATE = 8000
SECONDS = 2
TRACKS = {"thinmp_test_a": 440.0, "thinmp_test_b": 523.25}


def write(path: Path, frequency: float) -> None:
    frames = b"".join(
        struct.pack("<h", int(16000 * math.sin(2 * math.pi * frequency * i / SAMPLE_RATE)))
        for i in range(SAMPLE_RATE * SECONDS)
    )
    with wave.open(str(path), "wb") as out:
        out.setnchannels(1)
        out.setsampwidth(2)
        out.setframerate(SAMPLE_RATE)
        out.writeframes(frames)


if __name__ == "__main__":
    directory = Path(__file__).resolve().parent / "testdata"
    directory.mkdir(exist_ok=True)
    for name, frequency in TRACKS.items():
        path = directory / f"{name}.wav"
        write(path, frequency)
        print(f"wrote {path} ({path.stat().st_size} bytes)")
