package com.isa.jutjubic.dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TileVideoDto {

    private String tileId;
    private int zoom;
    private int x;
    private int y;

    // aggregate
    private int videoCount;
    private double centerLat;
    private double centerLng;

    // detailed
    private List<VideoPostDto> videos;

    private TileType type;
}

