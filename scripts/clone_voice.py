import sys
import json
import os
import argparse
import pyttsx3

def clone_voice(text, voice_sample_path, output_path, speaker_name="en_speaker_3"):
    try:
        if not text or not text.strip():
            print(json.dumps({"status": "error", "message": "Text is empty"}))
            return False

        engine = pyttsx3.init()
        engine.save_to_file(text, output_path)
        engine.runAndWait()

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

    args = parser.parse_args()
    success = clone_voice(args.text, args.voice_sample, args.output, args.speaker)
    sys.exit(0 if success else 1)