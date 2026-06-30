@echo off
set "MODELS_DIR=%1"
if "%MODELS_DIR%"=="" set "MODELS_DIR=..\models"

if not exist "%MODELS_DIR%" mkdir "%MODELS_DIR%"

if not exist "%MODELS_DIR%\vosk-model-ru-0.22" (
    echo Downloading Vosk model...
    curl -L -o "%MODELS_DIR%\vosk-model-ru-0.22.zip" https://alphacephei.com/vosk/models/vosk-model-ru-0.22.zip
    tar -xf "%MODELS_DIR%\vosk-model-ru-0.22.zip" -C "%MODELS_DIR%"
    del "%MODELS_DIR%\vosk-model-ru-0.22.zip"
    echo ✅ Model downloaded
) else (
    echo ✅ Model already exists
)