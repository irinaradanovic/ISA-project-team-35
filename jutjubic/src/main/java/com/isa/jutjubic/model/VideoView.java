package com.isa.jutjubic.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = {"video", "user"})
@Builder
@Entity
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "video_view", indexes = {
        @Index(name = "idx_view_video", columnList = "video_id"),
        @Index(name = "idx_view_date", columnList = "viewed_at")
})
public class VideoView {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Video koji je gledan
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "video_id", nullable = false)
    private VideoPost video;

    // Korisnik koji je gledao (može biti null ako dozvoliš anonimne)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "viewed_at", nullable = false)
    private LocalDateTime viewedAt;

    @PrePersist
    protected void onCreate() {
        if (this.viewedAt == null) {
            this.viewedAt = LocalDateTime.now();
        }
    }
}
