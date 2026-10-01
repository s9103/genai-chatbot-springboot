package com.sharad.chatbot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

/** Every error leaves the API as {"error":{"code":..., "message":...}} - never a stack trace. */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ResponseEntity<Map<String, Object>> body(int status, String code, String msg) {
        return ResponseEntity.status(status).body(Map.of("error", Map.of("code", code, "message", msg)));
    }

    @ExceptionHandler(ApiException.class)
    ResponseEntity<Map<String, Object>> api(ApiException e) { return body(e.getStatus(), e.getCode(), e.getMessage()); }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> invalid(MethodArgumentNotValidException e) {
        var f = e.getBindingResult().getFieldError();
        return body(400, "VALIDATION_ERROR", f == null ? "Invalid input" : f.getField() + ": " + f.getDefaultMessage());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<Map<String, Object>> badJson(HttpMessageNotReadableException e) { return body(400, "BAD_JSON", "Request body is missing or not valid JSON"); }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> other(Exception e) {
        log.error("Unhandled", e); // details go to logs, not to the client
        return body(500, "INTERNAL_ERROR", "Something went wrong");
    }
}
