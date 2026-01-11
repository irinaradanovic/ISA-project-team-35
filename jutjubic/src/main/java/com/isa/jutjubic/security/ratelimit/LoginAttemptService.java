package com.isa.jutjubic.security.ratelimit;

import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_TIME_MS = 60 * 1000; // 1 minut

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    public boolean isBlocked(String ip) {
        Attempt attempt = attempts.get(ip);
        if (attempt == null) return false;

        if (Instant.now().toEpochMilli() - attempt.firstAttemptTime > BLOCK_TIME_MS) {
            // Resetuj broj pokušaja posle isteka vremena
            attempts.remove(ip);
            return false;
        }

        return attempt.count >= MAX_ATTEMPTS;
    }

    public void loginSucceeded(String ip) {
        attempts.remove(ip);
    }

    public void loginFailed(String ip) {
        Attempt attempt = attempts.get(ip);
        long now = Instant.now().toEpochMilli();

        if (attempt == null || now - attempt.firstAttemptTime > BLOCK_TIME_MS) {
            attempts.put(ip, new Attempt(1, now));
        } else {
            attempt.count++;
        }
    }

    private static class Attempt {
        int count;
        long firstAttemptTime;

        public Attempt(int count, long firstAttemptTime) {
            this.count = count;
            this.firstAttemptTime = firstAttemptTime;
        }
    }

    // Helper: izvlačenje IP adrese iz HttpServletRequest
    public static String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0]; // prvi IP u lancu
    }
}
