package com.isa.jutjubic.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "popularVideos")
@Builder
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "etl_run", indexes = {
        @Index(name = "idx_etl_executed_at", columnList = "executed_at")
})
public class EtlRun {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "executed_at", nullable = false)
    private LocalDateTime executedAt;

    @Builder.Default
    @OneToMany(
            mappedBy = "etlRun",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<PopularVideo> popularVideos = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        if (this.executedAt == null) {
            this.executedAt = LocalDateTime.now();
        }
    }
}
