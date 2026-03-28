package com.talklingo.backend.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.talklingo.backend.dto.CreateSessionRequest;
import com.talklingo.backend.dto.SendMessageRequest;
import com.talklingo.backend.dto.SessionDetailsResponse;
import com.talklingo.backend.dto.TranslationMessageResponse;
import com.talklingo.backend.dto.WorkspaceStatsResponse;
import com.talklingo.backend.service.AuthenticatedUserService;
import com.talklingo.backend.service.TalkLingoSessionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final TalkLingoSessionService sessionService;
    private final AuthenticatedUserService authenticatedUserService;

    public SessionController(
            TalkLingoSessionService sessionService,
            AuthenticatedUserService authenticatedUserService) {
        this.sessionService = sessionService;
        this.authenticatedUserService = authenticatedUserService;
    }

    @GetMapping
    public List<SessionDetailsResponse> listSessions() {
        return sessionService.listSessions(authenticatedUserService.getRequiredEmail());
    }

    @GetMapping("/stats")
    public WorkspaceStatsResponse getWorkspaceStats() {
        return sessionService.getWorkspaceStats(authenticatedUserService.getRequiredEmail());
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionDetailsResponse createSession(@Valid @RequestBody CreateSessionRequest request) {
        return sessionService.createSession(authenticatedUserService.getRequiredEmail(), request);
    }

    @GetMapping("/{sessionId}")
    public SessionDetailsResponse getSession(@PathVariable Long sessionId) {
        return sessionService.getSession(authenticatedUserService.getRequiredEmail(), sessionId);
    }

    @PostMapping("/{sessionId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public TranslationMessageResponse sendMessage(
            @PathVariable Long sessionId,
            @Valid @RequestBody SendMessageRequest request) {
        return sessionService.sendMessage(authenticatedUserService.getRequiredEmail(), sessionId, request);
    }

    @PostMapping(path = "/{sessionId}/voice-messages", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public TranslationMessageResponse sendVoiceMessage(
            @PathVariable Long sessionId,
            @RequestParam String senderName,
            @RequestParam String sourceLanguage,
            @RequestParam String targetLanguage,
            @RequestParam("audio") MultipartFile audioFile) {
        return sessionService.sendVoiceMessage(
                authenticatedUserService.getRequiredEmail(),
                sessionId,
                senderName,
                sourceLanguage,
                targetLanguage,
                audioFile);
    }

}
