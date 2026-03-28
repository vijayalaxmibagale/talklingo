package com.talklingo.backend.service;

public class TranslationResult {

    private final String translatedText;
    private final String audioUrl;

    public TranslationResult(String translatedText, String audioUrl) {
        this.translatedText = translatedText;
        this.audioUrl = audioUrl;
    }

    public String getTranslatedText() {
        return translatedText;
    }

    public String getAudioUrl() {
        return audioUrl;
    }
}
