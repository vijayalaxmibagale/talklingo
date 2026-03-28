package com.talklingo.backend.service;

import java.util.Base64;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "talklingo.speech.provider", havingValue = "google")
public class GoogleCloudSpeechToTextProvider implements SpeechToTextProvider {

    private final RestClient restClient;
    private final String apiKey;

    public GoogleCloudSpeechToTextProvider(
            @Value("${talklingo.speech.google.base-url:https://speech.googleapis.com}") String baseUrl,
            @Value("${talklingo.translation.google.api-key:}") String apiKey) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "talklingo.translation.google.api-key must be set when talklingo.speech.provider=google");
        }

        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
        this.apiKey = apiKey;
    }

    @Override
    public String transcribe(byte[] audioBytes, String mimeType, String languageCode) {
        SpeechRecognitionRequest request = new SpeechRecognitionRequest(
                new SpeechRecognitionConfig(resolveEncoding(mimeType), 48000, normalizeLanguage(languageCode)),
                new SpeechRecognitionAudio(Base64.getEncoder().encodeToString(audioBytes)));

        SpeechRecognitionResponse response = restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/v1/speech:recognize").queryParam("key", apiKey).build())
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(SpeechRecognitionResponse.class);

        return extractTranscript(response);
    }

    private String normalizeLanguage(String languageCode) {
        return switch (languageCode == null ? "" : languageCode.trim().toLowerCase(Locale.ROOT)) {
            case "en" -> "en-US";
            case "es" -> "es-ES";
            case "fr" -> "fr-FR";
            case "de" -> "de-DE";
            case "hi" -> "hi-IN";
            case "ta" -> "ta-IN";
            case "ar" -> "ar-SA";
            case "ja" -> "ja-JP";
            default -> languageCode;
        };
    }

    private String resolveEncoding(String mimeType) {
        String normalizedType = mimeType == null ? "" : mimeType.toLowerCase(Locale.ROOT);
        if (normalizedType.contains("ogg")) {
            return "OGG_OPUS";
        }
        return "WEBM_OPUS";
    }

    private String extractTranscript(SpeechRecognitionResponse response) {
        if (response == null || response.results() == null || response.results().isEmpty()) {
            throw new IllegalArgumentException("No speech could be recognized from the recorded audio.");
        }

        return response.results().stream()
                .flatMap(result -> result.alternatives().stream())
                .map(SpeechRecognitionAlternative::transcript)
                .filter(transcript -> transcript != null && !transcript.isBlank())
                .reduce((first, second) -> first + " " + second)
                .orElseThrow(() -> new IllegalArgumentException("No speech could be recognized from the recorded audio."))
                .trim();
    }

    private record SpeechRecognitionRequest(SpeechRecognitionConfig config, SpeechRecognitionAudio audio) {
    }

    private record SpeechRecognitionConfig(String encoding, Integer sampleRateHertz, String languageCode) {
    }

    private record SpeechRecognitionAudio(String content) {
    }

    private record SpeechRecognitionResponse(List<SpeechRecognitionResult> results) {
    }

    private record SpeechRecognitionResult(List<SpeechRecognitionAlternative> alternatives) {
    }

    private record SpeechRecognitionAlternative(String transcript) {
    }
}
