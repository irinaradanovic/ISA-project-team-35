package com.isa.jutjubic.config;

import com.isa.jutjubic.model.Comment;
import com.isa.jutjubic.model.User;
import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.repository.CommentRepository;
import com.isa.jutjubic.repository.UserRepository;
import com.isa.jutjubic.repository.VideoPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Profile("local") // Pokreće se samo sa -Dspring.profiles.active=local || Run > Edit Configuration > Modify options > Add VM options
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VideoPostRepository videoPostRepository;
    private final CommentRepository commentRepository;

    @Override
    public void run(String... args) {

        User user1 = User.builder()
                .email("marko@test.com")
                .username("marko")
                .passwordHash("password") // kasnije samo ovde treba da stavimo hash funkciju
                .firstName("Marko")
                .lastName("Markovic")
                .address("Novi Sad")
                .active(true)
                .build();

        User user2 = User.builder()
                .email("ana@test.com")
                .username("ana")
                .passwordHash("password") // hash funkcija kada se napravi
                .firstName("Ana")
                .lastName("Anic")
                .address("Beograd")
                .active(true)
                .build();

        userRepository.saveAll(List.of(user1, user2));

        List<User> owners = List.of(user1, user2);
        int videoCount = 10;

        for (int i = 1; i <= videoCount; i++) {

            VideoPost videoPost = VideoPost.builder()
                    .title("VideoPost broj " + i)
                    .description("Ovo je test videoPost broj " + i)
                    .tags("tag" + i + ",test")
                    .thumbnailPath("/thumbnails/" + i + ".png")
                    .videoPath("/videos/" + i + ".mp4")
                    .createdAt(LocalDateTime.now())
                    .location(i % 2 == 0 ? "Beograd" : "Novi Sad")
                    .owner(owners.get(i % 2))
                    .build();

            videoPostRepository.save(videoPost);

            for (int j = 1; j <= 3; j++) {
                Comment comment = Comment.builder()
                        .content("Komentar " + j + " na VideoPost " + i)
                        .author(owners.get((i + j) % 2))
                        .videoPost(videoPost)
                        .build();
                commentRepository.save(comment);
            }
        }

        System.out.println("Local test data loaded");
    }
}
