package com.sharad.chatbot;

import jakarta.validation.Valid;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import com.sharad.chatbot.Dto.*;

@RestController @RequestMapping("/api/auth")
public class AuthController {
    private final UserRepo users; private final JwtService jwt;
    private final BCryptPasswordEncoder enc = new BCryptPasswordEncoder(); // salted, slow hash

    public AuthController(UserRepo users, JwtService jwt) { this.users = users; this.jwt = jwt; }

    @PostMapping("/register")
    public AuthRes register(@Valid @RequestBody AuthReq r) {
        String email = r.email().trim().toLowerCase();
        if (users.findByEmail(email).isPresent()) throw new ApiException(409, "EMAIL_TAKEN", "Email already registered");
        User u = users.save(new User(email, enc.encode(r.password())));
        return new AuthRes(jwt.create(u), u.getEmail());
    }

    @PostMapping("/login")
    public AuthRes login(@Valid @RequestBody AuthReq r) {
        User u = users.findByEmail(r.email().trim().toLowerCase()).orElse(null);
        // same message for "no user" and "wrong password" so attackers can't probe which emails exist
        if (u == null || !enc.matches(r.password(), u.getPasswordHash()))
            throw new ApiException(401, "BAD_CREDENTIALS", "Invalid email or password");
        return new AuthRes(jwt.create(u), u.getEmail());
    }
}
