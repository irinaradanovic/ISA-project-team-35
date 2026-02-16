package com.isa.jutjubic.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WatchPartyCreateRequestDto {
    private Integer videoId;
    private String roomName;
}