package com.isa.jutjubic.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WatchPartyDto {
    private Long id;
    private String roomName;
    private Long creatorId;
    private Integer currentVideoId;
    private boolean active;
}