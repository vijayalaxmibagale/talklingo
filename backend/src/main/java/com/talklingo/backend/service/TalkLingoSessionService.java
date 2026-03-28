package com.talklingo.backend.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.talklingo.backend.dto.CreateSessionRequest;
import com.talklingo.backend.dto.SendMessageRequest;
import com.talklingo.backend.dto.SessionDetailsResponse;
import com.talklingo.backend.dto.TranslationMessageResponse;
import com.talklingo.backend.entity.ConversationSession;
import com.talklingo.backend.entity.TranslationMessage;
import com.talklingo.backend.repository.ConversationSessionRepository;
import com.talklingo.backend.repository.TranslationMessageRepository;
import com.talklingo.backend.dto.WorkspaceStatsResponse;

@Service
public class TalkLingoSessionService {

    private final ConversationSessionRepository sessionRepository;
    private final TranslationMessageRepository messageRepository;
    private final TranslationProvider translationProvider;
    private final SpeechToTextProvider speechToTextProvider;
    private final SimpMessagingTemplate messagingTemplate;

    public TalkLingoSessionService(
            ConversationSessionRepository sessionRepository,
            TranslationMessageRepository messageRepository,
            TranslationProvider translationProvider,
            SpeechToTextProvider speechToTextProvider,
            SimpMessagingTemplate messagingTemplate) {
        this.sessionRepository = sessionRepository;
        this.messageRepository = messageRepository;
        this.translationProvider = translationProvider;
        this.speechToTextProvider = speechToTextProvider;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public SessionDetailsResponse createSession(String ownerEmail, CreateSessionRequest request) {
        ConversationSession session = new ConversationSession();
        session.setTitle(request.getTitle().trim());
        session.setSourceLanguage(request.getSourceLanguage().trim().toLowerCase());
        session.setTargetLanguage(request.getTargetLanguage().trim().toLowerCase());
        session.setOwnerEmail(ownerEmail.trim().toLowerCase());

        ConversationSession savedSession = sessionRepository.save(session);
        return mapSession(savedSession, List.of());
    }

    @Transactional(readOnly = true)
    public List<SessionDetailsResponse> listSessions(String ownerEmail) {
        return sessionRepository.findByOwnerEmailOrderByCreatedAtDesc(ownerEmail.trim().toLowerCase())
                .stream()
                .map(session -> mapSession(session, List.of()))
                .toList();
    }

    @Transactional(readOnly = true)
    public SessionDetailsResponse getSession(String ownerEmail, Long sessionId) {
        ConversationSession session = findSession(ownerEmail, sessionId);
        List<TranslationMessageResponse> messages = messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId)
                .stream()
                .map(this::mapMessage)
                .toList();
        return mapSession(session, messages);
    }

    @Transactional
    public TranslationMessageResponse sendMessage(String ownerEmail, Long sessionId, SendMessageRequest request) {
        ConversationSession session = findSession(ownerEmail, sessionId);
        TranslationResult translationResult = translationProvider.translate(
                request.getText().trim(),
                request.getSourceLanguage().trim(),
                request.getTargetLanguage().trim());

        TranslationMessage message = buildMessage(
                session,
                request.getSenderName().trim(),
                request.getSourceLanguage().trim().toLowerCase(),
                request.getTargetLanguage().trim().toLowerCase(),
                request.getText().trim(),
                translationResult);

        return saveAndBroadcast(sessionId, message);
    }

    @Transactional
    public TranslationMessageResponse sendVoiceMessage(
            String ownerEmail,
            Long sessionId,
            String senderName,
            String sourceLanguage,
            String targetLanguage,
            MultipartFile audioFile) {
        ConversationSession session = findSession(ownerEmail, sessionId);
        validateAudioFile(audioFile);

        String originalText;
        try {
            originalText = speechToTextProvider.transcribe(
                    audioFile.getBytes(),
                    audioFile.getContentType(),
                    sourceLanguage.trim());
        } catch (IOException exception) {
            throw new IllegalArgumentException("Could not read the uploaded audio.");
        }

        TranslationResult translationResult = translationProvider.translate(
                originalText,
                sourceLanguage.trim(),
                targetLanguage.trim());

        TranslationMessage message = buildMessage(
                session,
                senderName.trim(),
                sourceLanguage.trim().toLowerCase(),
                targetLanguage.trim().toLowerCase(),
                originalText,
                translationResult);

        return saveAndBroadcast(sessionId, message);
    }

    @Transactional(readOnly = true)
    public WorkspaceStatsResponse getWorkspaceStats(String ownerEmail) {
        String normalizedOwner = ownerEmail.trim().toLowerCase();
        List<ConversationSession> sessions = sessionRepository.findByOwnerEmailOrderByCreatedAtDesc(normalizedOwner);
        Map<String, Long> languagePairs = sessions.stream()
                .collect(Collectors.groupingBy(
                        session -> session.getSourceLanguage() + " -> " + session.getTargetLanguage(),
                        Collectors.counting()));

        WorkspaceStatsResponse response = new WorkspaceStatsResponse();
        response.setTotalSessions(sessionRepository.countByOwnerEmail(normalizedOwner));
        response.setTotalMessages(messageRepository.countBySessionOwnerEmail(normalizedOwner));
        response.setTopLanguagePair(languagePairs.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("No sessions yet"));
        return response;
    }

    private ConversationSession findSession(String ownerEmail, Long sessionId) {
        ConversationSession session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Session not found: " + sessionId));

        if (!session.getOwnerEmail().equalsIgnoreCase(ownerEmail)) {
            throw new IllegalArgumentException("Session not found: " + sessionId);
        }

        return session;
    }

    private TranslationMessage buildMessage(
            ConversationSession session,
            String senderName,
            String sourceLanguage,
            String targetLanguage,
            String originalText,
            TranslationResult translationResult) {
        TranslationMessage message = new TranslationMessage();
        message.setSession(session);
        message.setSenderName(senderName);
        message.setSourceLanguage(sourceLanguage);
        message.setTargetLanguage(targetLanguage);
        message.setOriginalText(originalText);
        message.setTranslatedText(translationResult.getTranslatedText());
        message.setAudioUrl(translationResult.getAudioUrl());
        return message;
    }

    private TranslationMessageResponse saveAndBroadcast(Long sessionId, TranslationMessage message) {
        TranslationMessage savedMessage = messageRepository.save(message);
        TranslationMessageResponse response = mapMessage(savedMessage);
        messagingTemplate.convertAndSend("/topic/sessions/" + sessionId, response);
        return response;
    }

    private void validateAudioFile(MultipartFile audioFile) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new IllegalArgumentException("Please record audio before sending.");
        }

        String contentType = audioFile.getContentType();
        if (contentType == null || !(contentType.startsWith("audio/webm") || contentType.startsWith("audio/ogg"))) {
            throw new IllegalArgumentException("Only WebM or OGG audio recordings are supported.");
        }
    }

    private SessionDetailsResponse mapSession(
            ConversationSession session,
            List<TranslationMessageResponse> messages) {
        SessionDetailsResponse response = new SessionDetailsResponse();
        response.setId(session.getId());
        response.setTitle(session.getTitle());
        response.setSourceLanguage(session.getSourceLanguage());
        response.setTargetLanguage(session.getTargetLanguage());
        response.setStatus(session.getStatus());
        response.setOwnerEmail(session.getOwnerEmail());
        response.setCreatedAt(session.getCreatedAt());
        response.setMessages(messages);
        return response;
    }

    private TranslationMessageResponse mapMessage(TranslationMessage message) {
        TranslationMessageResponse response = new TranslationMessageResponse();
        response.setId(message.getId());
        response.setSessionId(message.getSession().getId());
        response.setSenderName(message.getSenderName());
        response.setSourceLanguage(message.getSourceLanguage());
        response.setTargetLanguage(message.getTargetLanguage());
        response.setOriginalText(message.getOriginalText());
        response.setTranslatedText(message.getTranslatedText());
        response.setAudioUrl(message.getAudioUrl());
        response.setDeliveryStatus(message.getDeliveryStatus());
        response.setCreatedAt(message.getCreatedAt());
        return response;
    }
}
