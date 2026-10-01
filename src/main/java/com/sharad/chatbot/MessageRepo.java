package com.sharad.chatbot;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepo extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByUserIdOrderByIdDesc(Long userId, Pageable page); // newest N (history trimming)
    List<ChatMessage> findByUserIdOrderByIdAsc(Long userId);                 // full history for the UI
}
