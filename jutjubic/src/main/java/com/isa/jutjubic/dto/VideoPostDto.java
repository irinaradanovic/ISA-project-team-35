package com.isa.jutjubic.dto;

import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString

public class VideoPostDto {
    private Integer id;
    private String title;
    private String description;
    private String tags;
    private String videoPath;
    private String thumbnailPath;
    private LocalDateTime createdAt;
    private String location;
}

