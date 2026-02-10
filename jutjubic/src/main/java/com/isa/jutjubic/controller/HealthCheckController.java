package com.isa.jutjubic.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cluster")
public class HealthCheckController {

    @Autowired
    private DataSource dataSource;

    @Value("${server.port:8080}")
    private String port;

    @GetMapping("/health/custom")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("replica_port", port);

        try (Connection conn = dataSource.getConnection()) {
            health.put("database", conn.isValid(2) ? "UP" : "DOWN");
        } catch (Exception e) {
            health.put("database", "DOWN - " + e.getMessage());
            health.put("status", "DEGRADED");
        }
        return ResponseEntity.ok(health);
    }


    @GetMapping("/test/replica")
    public ResponseEntity<Map<String, String>> testReplica() {
        Map<String, String> response = new HashMap<>();
        response.put("message", "Zahtev obrađen");
        response.put("port", port);
        return ResponseEntity.ok(response);
    }
}