package com.isa.jutjubic.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class MapTile {

    @Id
    private String id;   //format: "z_x_y_range" (npr. "5_18_12_ALL")

    private int zoom;
    private int videoCount;
    private LocalDateTime lastUpdated;

    // sredisnja tacka kvadrata - za iscrtavanje markera na mapi
    private double centerLat;
    private double centerLng;

}
