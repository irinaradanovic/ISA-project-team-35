package com.isa.jutjubic.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
@Builder
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
    private String location;
    //private String username;  //U SLUCAJU DA NE BUDE RADILO SA SKRITPOM OSTAVLJAM ZBOG TESTIRANJA


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private int likeCount = 0;
    @Column(nullable = false)
    private int commentCount = 0;
    @Column(nullable = false)
    private int viewCount = 0;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }


}
