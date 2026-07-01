import sys
import os
sys.path.insert(0, os.path.dirname(__file__))

import tts_patch
import json
import argparse
import tempfile
tempfile.tempdir = "E:/VoiceSwapTemp/"

os.environ["HF_HOME"] = os.path.join(os.getcwd(), ".cache")

try:
    from TTS.api import TTS
except ImportError:
    print(json.dumps({"status": "error", "message": "TTS not installed. Run: pip install TTS"}))
    sys.exit(1)

def clone_voice(text, voice_sample_path, output_path, speaker_name="en_speaker_3"):
    """
    Клонирует голос и синтезирует речь с использованием TTS XTTS
    """
    try:
        if not text or not text.strip():
            print(json.dumps({"status": "error", "message": "Text is empty"}))
            return False

        if not os.path.exists(voice_sample_path):
            print(json.dumps({"status": "error", "message": f"Voice sample not found: {voice_sample_path}"}))
            return False

        print(json.dumps({"status": "info", "message": "Loading XTTS model..."}))
        sys.stdout.flush()

        # XTTS v2 для клонирования голоса
        tts = TTS(model_name="tts_models/multilingual/multi-dataset/xtts_v2", progress_bar=False)

        print(json.dumps({"status": "info", "message": "Synthesizing with voice cloning..."}))
        sys.stdout.flush()

        # Синтез с клонированием голоса
        tts.tts_to_file(
            text=text,
            speaker_wav=voice_sample_path,
            language="ru",
            file_path=output_path
        )

        print(json.dumps({"status": "success", "output": output_path}))
        return True

    except Exception as e:
        print(json.dumps({"status": "error", "message": str(e)}))
        return False

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--text", required=True, help="Text to synthesize")
    parser.add_argument("--voice-sample", required=True, help="Path to voice sample WAV file")
    parser.add_argument("--output", required=True, help="Output audio path")
    parser.add_argument("--speaker", default="en_speaker_3", help="Speaker name (ignored for XTTS)")
    parser.add_argument("--skip-license-check", action="store_true", help="Skip license confirmation")

    args = parser.parse_args()

    success = clone_voice(args.text, args.voice_sample, args.output, args.speaker)
    sys.exit(0 if success else 1)