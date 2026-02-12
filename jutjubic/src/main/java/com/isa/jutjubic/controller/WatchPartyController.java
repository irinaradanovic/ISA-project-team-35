package com.isa.jutjubic.controller;

import com.isa.jutjubic.dto.WatchPartyCreateRequestDto;
import com.isa.jutjubic.dto.WatchPartyDto;
import com.isa.jutjubic.service.WatchPartyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.NoSuchElementException;

@Controller
@RequestMapping("/api/watch-party")
@CrossOrigin(origins = "http://localhost:5173")
public class WatchPartyController {

    @Autowired
    private WatchPartyService watchPartyService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;


    @PostMapping
    @ResponseBody
    public ResponseEntity<?> create(@RequestBody WatchPartyCreateRequestDto request) {
        try {
            return ResponseEntity.ok(watchPartyService.createRoom(request));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<?> getRoom(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(watchPartyService.getRoom(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }


    @MessageMapping("/watch-party.start/{roomId}")
    public void start(
            @DestinationVariable Long roomId,
            Map<String, Object> payload
    ) {
        Integer videoId = ((Number) payload.get("videoId")).intValue();
        watchPartyService.markStarted(roomId, videoId);

        messagingTemplate.convertAndSend("/topic/watch-party/" + roomId,
                Map.of("type", "START", "roomId", roomId, "videoId", videoId)
        );
    }

    @MessageMapping("/watch-party.stop/{roomId}")
    public void stop(
            @DestinationVariable Long roomId,
            Map<String, Object> payload
    ) {
        watchPartyService.markStopped(roomId);

        messagingTemplate.convertAndSend("/topic/watch-party/" + roomId,
                Map.of("type", "STOP", "roomId", roomId)
        );
    }
}