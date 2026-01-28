package com.isa.jutjubic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TileVideoDto {
    private String tileId;
    private List<VideoPostDto> videos;
}
