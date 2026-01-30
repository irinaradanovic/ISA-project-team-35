package com.isa.jutjubic.dto;

import lombok.Data;
import java.util.List;

@Data
public class TileRequest {
    private List<String> tiles;  // npr. "5_18_12_ALL"

    private TimeRange timeRange;
    //S2 dodato za filter po vremenu
}
