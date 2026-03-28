package com.talklingo.backend.service;

public interface SpeechToTextProvider {

    String transcribe(byte[] audioBytes, String mimeType, String languageCode);
}
