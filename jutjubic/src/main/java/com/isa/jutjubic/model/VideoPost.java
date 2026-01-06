package com.isa.jutjubic.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Entity
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

    private String username; // mockovani korisnik

    //posle dodati i korisnika koji je postavio video
  /*  @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id")
    private RegisteredUser postCreator; */

}
