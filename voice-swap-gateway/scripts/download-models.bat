@echo off
if not exist "models" mkdir models

if not exist "models\vosk-model-ru-0.22" (
    echo Downloading Vosk model...
    curl -L -o models\vosk-model-ru-0.22.zip https://alphacephei.com/vosk/models/vosk-model-ru-0.22.zip
    tar -xf models\vosk-model-ru-0.22.zip -C models\
    del models\vosk-model-ru-0.22.zip
    echo ✅ Model downloaded
) else (
    echo ✅ Model already exists
)