package com.isa.jutjubic.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Cache;
import jakarta.persistence.Cacheable;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "\"comments\"",
        indexes = {
                @Index(name = "idx_comment_video_created", columnList = "video_id, createdAt"),
                @Index(name = "idx_comment_user_created", columnList = "user_id, createdAt")
        })
@Getter
@Setter
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id", nullable = false)
    @JsonIgnore
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
    private VideoPost videoPost;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable = false, updatable = false)
    private Date createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = new Date();
    }
}
