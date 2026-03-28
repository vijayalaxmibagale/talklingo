package com.talklingo.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.HtmlUtils;

@Component
@ConditionalOnProperty(name = "talklingo.translation.provider", havingValue = "google")
public class GoogleCloudTranslationProvider implements TranslationProvider {

    private final RestClient restClient;
    private final String apiKey;
    private final String mediaBaseUrl;

    public GoogleCloudTranslationProvider(
            @Value("${talklingo.translation.google.base-url:https://translation.googleapis.com}") String baseUrl,
            @Value("${talklingo.translation.google.api-key:}") String apiKey,
            @Value("${talklingo.media.base-url}") String mediaBaseUrl) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "talklingo.translation.google.api-key must be set when talklingo.translation.provider=google");
        }

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
        this.mediaBaseUrl = mediaBaseUrl;
    }

    @Override
    public TranslationResult translate(String text, String sourceLanguage, String targetLanguage) {
        String normalizedText = text == null ? "" : text.trim();
        String normalizedSource = sourceLanguage == null ? "" : sourceLanguage.trim().toLowerCase();
        String normalizedTarget = targetLanguage == null ? "" : targetLanguage.trim().toLowerCase();

        if (normalizedSource.equals(normalizedTarget)) {
            return new TranslationResult(normalizedText, mediaBaseUrl + "/browser-tts/" + normalizedTarget + ".mp3");
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("q", normalizedText);
        formData.add("source", normalizedSource);
        formData.add("target", normalizedTarget);
        formData.add("format", "text");

        GoogleTranslateResponse response = restClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/language/translate/v2")
                        .queryParam("key", apiKey)
                        .build())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(formData)
                .retrieve()
                .body(GoogleTranslateResponse.class);

        String translatedText = extractTranslatedText(response);
        return new TranslationResult(translatedText, mediaBaseUrl + "/browser-tts/" + normalizedTarget + ".mp3");
    }

    private String extractTranslatedText(GoogleTranslateResponse response) {
        if (response == null || response.data() == null || response.data().translations() == null
                || response.data().translations().isEmpty()) {
            throw new IllegalStateException("Google Translate returned an empty response.");
        }

        String translatedText = response.data().translations().get(0).translatedText();
        if (translatedText == null || translatedText.isBlank()) {
            throw new IllegalStateException("Google Translate did not return translated text.");
        }

        return HtmlUtils.htmlUnescape(translatedText);
    }

    private record GoogleTranslateResponse(GoogleTranslateData data) {
    }

    private record GoogleTranslateData(List<GoogleTranslateItem> translations) {
    }

    private record GoogleTranslateItem(String translatedText) {
    }
}
