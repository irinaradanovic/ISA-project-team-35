package com.isa.jutjubic.controller;

import com.isa.jutjubic.dto.TimeRange;
import com.isa.jutjubic.service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173") // front URL
@RequestMapping("/api/map")
public class MapController {

    @Autowired
    private MapService mapService;

    // Filtracija po vremenu
    @GetMapping("/videos")
    public ResponseEntity<?> getVideosForMap(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLng,
            @RequestParam double maxLng,
            @RequestParam(defaultValue = "ALL") TimeRange timeRange
    ) {
        // Validacija
        if (minLat > maxLat || minLng > maxLng) {
            return ResponseEntity.badRequest().body("Invalid bounding box");
        }
        if (minLat < -90 || maxLat > 90 || minLng < -180 || maxLng > 180) {
            return ResponseEntity.badRequest().body("Invalid coordinates");
        }
        return ResponseEntity.ok(
                mapService.getVideosForMap(
                        minLat, maxLat, minLng, maxLng, timeRange
                )
        );
    }
}
