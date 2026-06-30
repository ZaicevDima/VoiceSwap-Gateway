#!/bin/bash

MODELS_DIR="${1:-../models}"

mkdir -p "$MODELS_DIR"

if [ ! -d "$MODELS_DIR/vosk-model-ru-0.22" ]; then
    echo "Downloading Vosk model..."

    curl -L -o "$MODELS_DIR/vosk-model-ru-0.22.zip" https://alphacephei.com/vosk/models/vosk-model-ru-0.22.zip

    echo "Extracting..."
    unzip -q "$MODELS_DIR/vosk-model-ru-0.22.zip" -d "$MODELS_DIR"

    echo "Cleaning up..."
    rm "$MODELS_DIR/vosk-model-ru-0.22.zip"

    echo "✅ Model downloaded to $MODELS_DIR"
else
    echo "✅ Model already exists in $MODELS_DIR"
fi