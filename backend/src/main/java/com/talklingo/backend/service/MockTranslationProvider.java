package com.talklingo.backend.service;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "talklingo.translation.provider", havingValue = "mock", matchIfMissing = true)
public class MockTranslationProvider implements TranslationProvider {

    private final String mediaBaseUrl;

    public MockTranslationProvider(@Value("${talklingo.media.base-url}") String mediaBaseUrl) {
        this.mediaBaseUrl = mediaBaseUrl;
    }

    @Override
    public TranslationResult translate(String text, String sourceLanguage, String targetLanguage) {
        String normalizedSource = sourceLanguage.toUpperCase(Locale.ROOT);
        String normalizedTarget = targetLanguage.toUpperCase(Locale.ROOT);
        String translatedText = "[" + normalizedSource + "->" + normalizedTarget + "] " + text;
        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString((translatedText + "|" + normalizedTarget).getBytes(StandardCharsets.UTF_8));
        return new TranslationResult(translatedText, mediaBaseUrl + "/" + token + ".mp3");
    }
}
