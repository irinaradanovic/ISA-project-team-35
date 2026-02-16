package com.isa.jutjubic.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PopularVideoDto {

    private Integer videoId;
    private String title;
    private String thumbnailPath;
    private Long popularityScore;
    private Integer rankPosition;
}
