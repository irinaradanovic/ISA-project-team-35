package com.isa.jutjubic.security.jwt;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class UserTracker {

    // Mapa koja čuva email korisnika i vreme poslednje aktivnosti
    private final Map<String, Long> activeUsersMap = new ConcurrentHashMap<>();

    public UserTracker(MeterRegistry registry) {
        // Registrujemo metriku u Prometheus pod imenom "app_users_active"
        // Vrednost će biti broj elemenata u mapi
        Gauge.builder("app_users_active", activeUsersMap, Map::size)
                .description("Broj korisnika aktivnih u poslednjih 5 minuta")
                .register(registry);
    }

    public void logActivity(String email) {
        activeUsersMap.put(email, System.currentTimeMillis());
        cleanup(); // Očisti stare sesije
    }

    private void cleanup() {
        long fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000);
        activeUsersMap.entrySet().removeIf(entry -> entry.getValue() < fiveMinutesAgo);
    }
}