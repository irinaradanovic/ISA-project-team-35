package com.isa.jutjubic.dto;

import lombok.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString

public class VideoPostDto {
    //za prikaz
    private Integer id;
    private String title;
    private String description;
    private String tags;
    private String videoPath;
    private String thumbnailPath;
    private LocalDateTime createdAt;

    private String city;
    private String country;
    private Double latitude;   //za mapu
    private Double longitude;

    private String ownerUsername;
    private Long ownerId;
    private int likeCount;
    private int commentCount;
    private int viewCount;
}

