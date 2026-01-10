package com.isa.jutjubic.controller;

import com.isa.jutjubic.dto.*;
import com.isa.jutjubic.security.jwt.JwtTokenProvider;
import com.isa.jutjubic.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    // =========================
    // REGISTRACIJA + EMAIL AKTIVACIJA
    // =========================
    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest servletRequest) {

        // Kreiramo osnovni URL aplikacije da bismo napravili link za aktivaciju
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
    // LOGIN + JWT
    // =========================
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {

        // Autentifikacija korisnika (email + lozinka)
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getEmail(),
                                request.getPassword()
                        )
                );

        // Generišemo JWT token
        String token = tokenProvider.generateToken(authentication);

        return ResponseEntity.ok(new LoginResponse(token));
    }
}
