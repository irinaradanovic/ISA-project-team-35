package com.isa.jutjubic.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class PopularVideoDto {

    private Integer videoId;
    private String title;
    private String thumbnailPath;
    private Long popularityScore;
    private Integer rankPosition;

    // Dodatna polja potrebna za frontend
    private Long ownerId;
    private String ownerUsername;
    private LocalDateTime createdAt;
    private Long viewCount;
}
