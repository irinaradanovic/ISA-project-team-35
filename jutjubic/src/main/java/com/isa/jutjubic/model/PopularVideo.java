package com.isa.jutjubic.model;
import jakarta.persistence.Entity;
import lombok.*;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "popular_videos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class PopularVideo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "video_id")
    private VideoPost video;

    private Double popularityScore;
    private LocalDateTime calculationDate; // Kada je pokrenut ETL
}
