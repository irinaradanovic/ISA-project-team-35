package com.isa.jutjubic.model;
import lombok.*;
import jakarta.persistence.*;

@Entity
@Table(name = "watch_parties")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class WatchParty {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String roomName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToOne
    private VideoPost currentVideo;

    private boolean isActive = false;

}