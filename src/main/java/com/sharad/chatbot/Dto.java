package com.sharad.chatbot;

import jakarta.validation.constraints.*;

/** Request/response shapes. Records = immutable data carriers. */
public class Dto {
    public record AuthReq(@NotBlank @Email String email, @NotBlank @Size(min = 6, max = 72) String password) {}
    public record AuthRes(String token, String email) {}
    public record ChatReq(@NotBlank @Size(max = 2000) String message) {}
    public record ChatRes(String reply) {}
    public record MsgView(String role, String content) {}
}
