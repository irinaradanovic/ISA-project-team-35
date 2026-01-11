package com.isa.jutjubic.service;

import com.isa.jutjubic.dto.RegisterRequest;
import com.isa.jutjubic.model.User;
import com.isa.jutjubic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    // Za test: token -> email map
    private final Map<String, String> activationTokens = new HashMap<>();

    public void register(RegisterRequest request, String appUrl) {

        // 1. Proveri da li email postoji
        Optional<User> existing = userRepository.findByEmail(request.getEmail());
        if(existing.isPresent()) {
            throw new IllegalArgumentException("Email already in use");
        }

        // 2. Proveri da li password i confirmPassword poklapaju
        if(!request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // 3. Kreiraj korisnika
        User user = User.builder()
                .email(request.getEmail())
                .username(request.getUsername())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .address(request.getAddress())
                .active(false) // neće moći da se prijavi dok ne klikne aktivaciju
                .build();

        userRepository.save(user);

        // 4. Kreiraj token i pošalji email
        String token = UUID.randomUUID().toString();
        activationTokens.put(token, user.getEmail());

        sendActivationEmail(user.getEmail(), token, appUrl);
    }

    private void sendActivationEmail(String email, String token, String appUrl) {
        String activationUrl = appUrl + "/api/auth/activate?token=" + token;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("Activate your account");
        mailMessage.setText("Click the link to activate your account: " + activationUrl);

        mailSender.send(mailMessage);
    }

    // =====================
    // NOVO: aktivacija naloga
    // =====================
    public void activateAccount(String token) {
        String email = activationTokens.get(token);
        if(email == null) {
            throw new IllegalArgumentException("Invalid activation token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setActive(true);
        userRepository.save(user);

        // ukloni token nakon aktivacije
        activationTokens.remove(token);
    }
    //prebaceno iz comment servic-a moze da se koristi na vise mesta
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("User not authenticated");
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
