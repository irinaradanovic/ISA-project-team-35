package com.isa.jutjubic.security.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TileBounds {
    private double minLat;
    private double maxLat;
    private double minLng;
    private double maxLng;
}
