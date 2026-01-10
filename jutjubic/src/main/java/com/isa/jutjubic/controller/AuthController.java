package com.isa.jutjubic.controller;

import com.isa.jutjubic.dto.*;
import com.isa.jutjubic.security.jwt.JwtTokenProvider;
import com.isa.jutjubic.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    // =========================
    // IP RATE LIMITING CONFIG
    // =========================
    private static final int MAX_ATTEMPTS_PER_MINUTE = 5;
    private final Map<String, AttemptInfo> loginAttempts = new ConcurrentHashMap<>();

    private record AttemptInfo(int count, Instant lastAttemptTime) {}

    private boolean isAllowed(String ip) {
        AttemptInfo attempt = loginAttempts.get(ip);
        Instant now = Instant.now();

        if (attempt == null || now.isAfter(attempt.lastAttemptTime.plusSeconds(60))) {
            // Resetujemo broj pokušaja ako je prošla minuta
            loginAttempts.put(ip, new AttemptInfo(1, now));
            return true;
        } else if (attempt.count < MAX_ATTEMPTS_PER_MINUTE) {
            loginAttempts.put(ip, new AttemptInfo(attempt.count + 1, attempt.lastAttemptTime));
            return true;
        } else {
            return false;
        }
    }

    // =========================
    // REGISTRACIJA + EMAIL AKTIVACIJA
    // =========================
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest) {

        String appUrl = servletRequest.getRequestURL().toString()
                .replace(servletRequest.getRequestURI(), "");

        authService.register(request, appUrl);
        return ResponseEntity.ok("User registered successfully. Check your email for activation link.");
    }

    @GetMapping("/activate")
    public ResponseEntity<String> activateAccount(@RequestParam String token) {
        authService.activateAccount(token);
        return ResponseEntity.ok("Account activated successfully!");
    }

    // =========================
    // LOGIN + JWT + RATE LIMITING
    // =========================
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        String clientIp = servletRequest.getRemoteAddr();

        if (!isAllowed(clientIp)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Too many login attempts from this IP. Please try again later.");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );

            String token = tokenProvider.generateToken(authentication);
            return ResponseEntity.ok(new LoginResponse(token));

        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
        }
    }
}
