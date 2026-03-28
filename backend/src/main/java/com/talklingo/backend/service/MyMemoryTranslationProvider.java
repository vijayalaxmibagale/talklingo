package com.talklingo.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.HtmlUtils;

@Component
@ConditionalOnProperty(name = "talklingo.translation.provider", havingValue = "mymemory")
public class MyMemoryTranslationProvider implements TranslationProvider {

    private final RestClient restClient;
    private final String mediaBaseUrl;

    public MyMemoryTranslationProvider(
            @Value("${talklingo.translation.mymemory.base-url:https://api.mymemory.translated.net}") String baseUrl,
            @Value("${talklingo.media.base-url}") String mediaBaseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.mediaBaseUrl = mediaBaseUrl;
    }

    @Override
    public TranslationResult translate(String text, String sourceLanguage, String targetLanguage) {
        String normalizedText = text == null ? "" : text.trim();
        String normalizedSource = sourceLanguage == null ? "" : sourceLanguage.trim().toLowerCase();
        String normalizedTarget = targetLanguage == null ? "" : targetLanguage.trim().toLowerCase();

        if (normalizedText.isBlank() || normalizedSource.equals(normalizedTarget)) {
            return new TranslationResult(normalizedText, mediaBaseUrl + "/browser-tts/" + normalizedTarget + ".mp3");
        }

        MyMemoryResponse response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/get")
                        .queryParam("q", normalizedText)
                        .queryParam("langpair", normalizedSource + "|" + normalizedTarget)
                        .build())
                .retrieve()
                .body(MyMemoryResponse.class);

        String translatedText = extractTranslatedText(response, normalizedText);
        return new TranslationResult(translatedText, mediaBaseUrl + "/browser-tts/" + normalizedTarget + ".mp3");
    }

    private String extractTranslatedText(MyMemoryResponse response, String fallbackText) {
        if (response == null || response.responseData() == null) {
            return fallbackText;
        }

        String translatedText = response.responseData().translatedText();
        if (translatedText == null || translatedText.isBlank()) {
            return fallbackText;
        }

        return HtmlUtils.htmlUnescape(translatedText);
    }

    private record MyMemoryResponse(MyMemoryData responseData) {
    }

    private record MyMemoryData(String translatedText) {
    }
}
