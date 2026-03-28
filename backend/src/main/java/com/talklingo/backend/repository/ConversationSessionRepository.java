package com.talklingo.backend.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.talklingo.backend.entity.ConversationSession;

public interface ConversationSessionRepository extends JpaRepository<ConversationSession, Long> {

    List<ConversationSession> findByOwnerEmailOrderByCreatedAtDesc(String ownerEmail);

    long countByOwnerEmail(String ownerEmail);
}
