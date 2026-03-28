package com.talklingo.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.talklingo.backend.entity.TranslationMessage;

public interface TranslationMessageRepository extends JpaRepository<TranslationMessage, Long> {

    List<TranslationMessage> findBySessionIdOrderByCreatedAtAsc(Long sessionId);

    long countBySessionOwnerEmail(String ownerEmail);
}
