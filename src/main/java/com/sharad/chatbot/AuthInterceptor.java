package com.sharad.chatbot;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Runs BEFORE protected controllers: reads "Authorization: Bearer <jwt>", puts userId on the request. */
@Component
public class AuthInterceptor implements HandlerInterceptor {
    private final JwtService jwt;
    public AuthInterceptor(JwtService jwt) { this.jwt = jwt; }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object handler) {
        String h = req.getHeader("Authorization");
        if (h == null || !h.startsWith("Bearer ")) throw new ApiException(401, "UNAUTHORIZED", "Please log in");
        try {
            req.setAttribute("userId", jwt.verify(h.substring(7)));
            return true;
        } catch (Exception e) {
            throw new ApiException(401, "INVALID_TOKEN", "Session expired or invalid. Log in again");
        }
    }
}
