import sys
import json
import os
import argparse
import tempfile

try:
    from speechcraft import text2voice, voice2embedding
except ImportError:
    print(json.dumps({"status": "error", "message": "speechcraft not installed. Run: pip install speechcraft"}))
    sys.exit(1)

def clone_voice(text, voice_sample_path, output_path, speaker_name="en_speaker_3"):
    """
    Клонирует голос и синтезирует речь
    """
    try:
        # Валидация
        if not text or not text.strip():
            print(json.dumps({"status": "error", "message": "Text is empty"}))
            return False

        if not os.path.exists(voice_sample_path):
            print(json.dumps({"status": "error", "message": f"Voice sample not found: {voice_sample_path}"}))
            return False

        # Загружаем эмбеддинг из голосового сэмпла
        embedding = voice2embedding(audio_file=voice_sample_path, voice_name="cloned_voice")

        # Синтезируем речь
        audio_numpy, sample_rate = text2voice(
            text,
            voice=embedding,
            speaker_name=speaker_name
        )

        # Сохраняем результат
        from media_toolkit import AudioFile
        audio = AudioFile().from_np_array(audio_numpy, sr=sample_rate)
        audio.save(output_path)

        print(json.dumps({"status": "success", "output": output_path}))
        return True

    except Exception as e:
        print(json.dumps({"status": "error", "message": str(e)}))
        return False

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--text", required=True, help="Text to synthesize")
    parser.add_argument("--voice-sample", required=True, help="Path to voice sample")
    parser.add_argument("--output", required=True, help="Output audio path")
    parser.add_argument("--speaker", default="en_speaker_3", help="Speaker name")

    args = parser.parse_args()

    success = clone_voice(args.text, args.voice_sample, args.output, args.speaker)
    sys.exit(0 if success else 1)