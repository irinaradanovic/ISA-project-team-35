package com.isa.jutjubic.model;

import jakarta.persistence.*;
import lombok.*;

import org.hibernate.annotations.Cache;
import java.time.LocalDateTime;
import java.util.Date;
import org.hibernate.annotations.Where;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter  @ToString
@Entity
@Builder
@Cacheable @Cache(usage = org.hibernate.annotations.CacheConcurrencyStrategy.READ_WRITE)
@Where(clause = "deleted = false") //Hibernate ignorise sve sto je obrisano

@Table(name = "video_post", indexes = {
        @Index(name = "idx_location", columnList = "latitude, longitude"),
        @Index(name = "idx_created", columnList = "created_at")
})
public class VideoPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String title;
    private String description;
    private String tags;
    @Column(name = "thumbnail_path")
    private String thumbnailPath;
    @Column(name = "video_path")
    private String videoPath;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Embedded
    private GeoLocation location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private int likeCount = 0;
    @Column(nullable = false)
    private int commentCount = 0;
    @Column(nullable = false)
    private int viewCount = 0;

    @Column(nullable = false)
    private boolean deleted = false;

    // Za zakazani prikaz
    private LocalDateTime scheduledAt = null;
    private boolean isStreaming = false;

    // Status transcoding-a (da se ne prikazuje dok nije spreman)
    @Builder.Default
    @Enumerated(EnumType.STRING)
    private VideoStatus status=VideoStatus.PENDING; // PENDING, PROCESSING, READY, FAILED

    @PrePersist
    protected void onCreate() {
        if(this.createdAt == null){
            this.createdAt = LocalDateTime.now();
        }
        if (status == null) status = VideoStatus.PENDING;
    }

    public enum VideoStatus {
        PENDING,    // Video je otpremljen, ceka u redu za transcoding
        PROCESSING, // Jedan od dva potrosaca (consumera) trenutno obradjuje video
        READY,      // Transcoding zavrsen, video je dostupan za gledanje
        FAILED      // Doslo je do greske prilikom transcodinga ili uploada
    }


}
