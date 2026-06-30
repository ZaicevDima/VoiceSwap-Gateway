#!/bin/bash
mkdir -p models

if [ ! -d "models/vosk-model-ru-0.22" ]; then
    echo "Downloading Vosk model..."
    wget -O models/vosk-model-ru-0.22.zip https://alphacephei.com/vosk/models/vosk-model-ru-0.22.zip
    unzip models/vosk-model-ru-0.22.zip -d models/
    rm models/vosk-model-ru-0.22.zip
    echo "✅ Model downloaded"
else
    echo "✅ Model already exists"
fi