package com.sharad.chatbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.*;

@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);
    private static final int MAX_ATTEMPTS = 3;

    private final RestClient client;
    private final String model;
    private final int maxTokens;
    private final boolean mock;
    private final String apiKey;

    public LlmService(
            @Value("${llm.base-url}") String baseUrl,
            @Value("${llm.api-key}") String apiKey,
            @Value("${llm.model}") String model,
            @Value("${llm.max-tokens}") int maxTokens) {

        var factory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(5))
                        .build()
        );

        factory.setReadTimeout(Duration.ofSeconds(30));

        this.client = RestClient.builder()
        .baseUrl(baseUrl)
        .requestFactory(factory)
        .defaultHeader("x-goog-api-key", apiKey)
        .build();

        this.model = model;
        this.maxTokens = maxTokens;
        this.apiKey = apiKey;
        this.mock = apiKey.isBlank();
    }

    @SuppressWarnings("unchecked")
    public String chat(List<Map<String, String>> messages) {

        if (mock) {
            return "[mock reply - set GEMINI_API_KEY for real answers] You said: "
                    + messages.get(messages.size() - 1).get("content");
        }

        // Convert our existing chat messages into Gemini format
        List<Map<String, Object>> contents = new ArrayList<>();

        for (Map<String, String> message : messages) {

            String role = message.get("role");
            String content = message.get("content");

            // Gemini uses "user" and "model"
            String geminiRole = "assistant".equals(role) ? "model" : "user";

            Map<String, Object> part = Map.of(
                    "text", content
            );

            contents.add(Map.of(
                    "role", geminiRole,
                    "parts", List.of(part)
            ));
        }

        Map<String, Object> generationConfig = Map.of(
                "maxOutputTokens", maxTokens,
                "temperature", 0.7
        );

        Map<String, Object> body = Map.of(
                "contents", contents,
                "generationConfig", generationConfig
        );

        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {

            try {

                Map<String, Object> response = client.post()
                        .uri(uriBuilder -> uriBuilder
        .path("/v1beta/models/{model}:generateContent")
        .build(model))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve()
                        .body(Map.class);

                List<Map<String, Object>> candidates =
                        (List<Map<String, Object>>) response.get("candidates");

                if (candidates == null || candidates.isEmpty()) {
                    throw new ApiException(
                            502,
                            "LLM_EMPTY_RESPONSE",
                            "The AI service returned an empty response"
                    );
                }

                Map<String, Object> content =
                        (Map<String, Object>) candidates.get(0).get("content");

                List<Map<String, Object>> parts =
                        (List<Map<String, Object>>) content.get("parts");

                if (parts == null || parts.isEmpty()) {
                    throw new ApiException(
                            502,
                            "LLM_EMPTY_RESPONSE",
                            "The AI service returned an empty response"
                    );
                }

                return (String) parts.get(0).get("text");

            } catch (RestClientResponseException e) {

                int status = e.getStatusCode().value();

                boolean retryable = status == 429 || status >= 500;

             log.warn(
        "Gemini HTTP {} (attempt {}/{}): {}",
        status,
        attempt,
        MAX_ATTEMPTS,
        e.getResponseBodyAsString()
);

                if (!retryable) {
                    throw new ApiException(
                            502,
                            "LLM_REJECTED",
                            "The AI service rejected the request"
                    );
                }

                if (attempt == MAX_ATTEMPTS) {
                    throw new ApiException(
                            503,
                            status == 429
                                    ? "LLM_RATE_LIMITED"
                                    : "LLM_UNAVAILABLE",
                            "The AI service is busy. Try again shortly"
                    );
                }

            } catch (ResourceAccessException e) {

                log.warn(
                        "Gemini network error (attempt {}/{}): {}",
                        attempt,
                        MAX_ATTEMPTS,
                        e.getMessage()
                );

                if (attempt == MAX_ATTEMPTS) {
                    throw new ApiException(
                            504,
                            "LLM_TIMEOUT",
                            "The AI service did not respond in time"
                    );
                }
            }

            try {
                Thread.sleep(1000L * attempt);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();

                throw new ApiException(
                        500,
                        "INTERRUPTED",
                        "Request interrupted"
                );
            }
        }

        throw new ApiException(
                500,
                "LLM_ERROR",
                "Unexpected AI error"
        );
    }
}