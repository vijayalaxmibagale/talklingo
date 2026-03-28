package com.talklingo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateSessionRequest {

    @NotBlank
    @Size(max = 120)
    private String title;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z-]{2,10}$")
    private String sourceLanguage;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z-]{2,10}$")
    private String targetLanguage;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }
}
