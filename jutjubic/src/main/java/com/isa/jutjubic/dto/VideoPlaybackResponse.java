package com.isa.jutjubic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class VideoPlaybackResponse {

    private Integer videoId;
    private String videoPath;
    private long offsetSeconds;
    private LocalDateTime scheduledAt;
}