package com.talklingo.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class SendMessageRequest {

    @NotBlank
    @Size(max = 120)
    private String senderName;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z-]{2,10}$")
    private String sourceLanguage;

    @NotBlank
    @Pattern(regexp = "^[a-zA-Z-]{2,10}$")
    private String targetLanguage;

    @NotBlank
    @Size(max = 2000)
    private String text;

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
