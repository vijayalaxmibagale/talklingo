package com.talklingo.backend.service;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "talklingo.speech.provider", havingValue = "mock", matchIfMissing = true)
public class MockSpeechToTextProvider implements SpeechToTextProvider {

    @Override
    public String transcribe(byte[] audioBytes, String mimeType, String languageCode) {
        throw new IllegalArgumentException(
                "Voice transcription is not enabled. Set TALKLINGO_SPEECH_PROVIDER=google and GOOGLE_TRANSLATE_API_KEY.");
    }
}
