package com.isa.jutjubic.config;

import com.isa.jutjubic.model.Comment;
import com.isa.jutjubic.model.User;
import com.isa.jutjubic.model.Video;
import com.isa.jutjubic.repository.CommentRepository;
import com.isa.jutjubic.repository.UserRepository;
import com.isa.jutjubic.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Profile("local")
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final CommentRepository commentRepository;

    @Override
    public void run(String... args) {

        // USERS
        User user1 = User.builder()
                .email("marko@test.com")
                .username("marko")
                .passwordHash("password")
                .firstName("Marko")
                .lastName("Markovic")
                .address("Novi Sad")
                .active(true)
                .build();

        User user2 = User.builder()
                .email("ana@test.com")
                .username("ana")
                .passwordHash("password")
                .firstName("Ana")
                .lastName("Anic")
                .address("Beograd")
                .active(true)
                .build();

        userRepository.saveAll(List.of(user1, user2));

        List<User> owners = List.of(user1, user2);
        int videoCount = 11; // prvi video + 10 novih

        for (int i = 1; i <= videoCount; i++) {

            Video video = Video.builder()
                    .title("Video broj " + i)
                    .description("Ovo je test video broj " + i)
                    .tags("tag" + i + ",test")
                    .thumbnailPath("/thumbnails/" + i + ".png")
                    .videoPath("/videos/" + i + ".mp4")
                    .owner(owners.get(i % 2))
                    .location(i % 2 == 0 ? "Beograd" : "Novi Sad")
                    .build();

            videoRepository.save(video);

            for (int j = 1; j <= 5; j++) {
                Comment comment = Comment.builder()
                        .content("Komentar " + j + " na video " + i)
                        .author(owners.get((i + j) % 2))
                        .video(video)
                        .build();
                commentRepository.save(comment);
            }
        }
        System.out.println("Local test data loaded ✔");
    }
}

