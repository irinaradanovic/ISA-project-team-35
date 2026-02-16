package com.isa.jutjubic.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"etlRun", "video"})
@Builder
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(
        name = "popular_video",
        indexes = {
                @Index(name = "idx_popular_video_score", columnList = "popularity_score")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_etl_run_rank",
                        columnNames = {"etl_run_id", "rank_position"}
                )
        }
)
public class PopularVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Veza ka ETL izvršavanju
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "etl_run_id", nullable = false)
    private EtlRun etlRun;

    // Video koji je popularan
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    private VideoPost video;

    @Column(name = "popularity_score", nullable = false)
    private Long popularityScore;

    @Column(name = "rank_position", nullable = false)
    private Integer rankPosition;
}
