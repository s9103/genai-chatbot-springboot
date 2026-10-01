package com.sharad.chatbot;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false) private Long userId;
    @Column(nullable = false) private String role;               // "user" or "assistant"
    @Column(nullable = false, columnDefinition = "TEXT") private String content;
    private Instant createdAt = Instant.now();
    protected ChatMessage() {}
    public ChatMessage(Long userId, String role, String content) { this.userId = userId; this.role = role; this.content = content; }
    public String getRole() { return role; }
    public String getContent() { return content; }
}
