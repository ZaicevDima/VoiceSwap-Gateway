import sys
import os
import json
import argparse
import soundfile as sf
from qwen_tts import Qwen3TTSModel

def clone_voice(text, voice_sample_path, output_path, speaker_name="en_speaker_3"):
    # Нормализуем пути для Windows
    voice_sample_path = os.path.normpath(voice_sample_path)
    output_path = os.path.normpath(output_path)

    try:
        if not text or not text.strip():
            print(json.dumps({"status": "error", "message": "Text is empty"}))
            return False

        if not os.path.exists(voice_sample_path):
            print(json.dumps({"status": "error", "message": f"Voice sample not found: {voice_sample_path}"}))
            return False

        print(json.dumps({"status": "info", "message": "Loading Qwen3-TTS model..."}))
        sys.stdout.flush()

        model = Qwen3TTSModel.from_pretrained(
            "Qwen/Qwen3-TTS-12Hz-0.6B-Base",
            device_map="cpu",
        )

        print(json.dumps({"status": "info", "message": "Synthesizing with voice cloning..."}))
        sys.stdout.flush()

        wavs, sr = model.generate_voice_clone(
            text=text,
            language="Russian",
            ref_audio=voice_sample_path,
            ref_text=text,
        )

        sf.write(output_path, wavs[0], sr)

        print(json.dumps({"status": "success", "output": output_path}))
        return True

    except Exception as e:
        print(json.dumps({"status": "error", "message": str(e)}))
        return False

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--text", required=True)
    parser.add_argument("--voice-sample", required=True)
    parser.add_argument("--output", required=True)
    parser.add_argument("--speaker", default="en_speaker_3")
    parser.add_argument("--skip-license-check", action="store_true")

    args = parser.parse_args()
    success = clone_voice(args.text, args.voice_sample, args.output, args.speaker)
    sys.exit(0 if success else 1)