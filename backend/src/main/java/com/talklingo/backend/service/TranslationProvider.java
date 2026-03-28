package com.talklingo.backend.service;

public interface TranslationProvider {

    TranslationResult translate(String text, String sourceLanguage, String targetLanguage);
}
