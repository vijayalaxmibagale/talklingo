package com.talklingo.backend.dto;

import java.time.Instant;
import java.util.List;

public class SessionDetailsResponse {

    private Long id;
    private String title;
    private String sourceLanguage;
    private String targetLanguage;
    private String status;
    private String ownerEmail;
    private Instant createdAt;
    private List<TranslationMessageResponse> messages;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<TranslationMessageResponse> getMessages() {
        return messages;
    }

    public void setMessages(List<TranslationMessageResponse> messages) {
        this.messages = messages;
    }
}
