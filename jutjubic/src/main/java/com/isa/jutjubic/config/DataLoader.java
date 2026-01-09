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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.Math.random;


@Component
@Profile("local") // Pokreće se samo sa -Dspring.profiles.active=local || Run > Edit Configuration > Modify options > Add VM options
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VideoPostRepository videoPostRepository;
    private final CommentRepository commentRepository;

   /* private static final List<String> AVAILABLE_TAGS = List.of(
            "Music",
            "Vlog",
            "Travel",
            "Food",
            "Education",
            "Gaming",
            "Comedy",
            "Fitness"
    );

    private static final List<String> LOCATIONS = List.of(
            "Beograd",
            "Novi Sad",
            "Niš",
            "Kragujevac",
            "Subotica",
            "Zagreb",
            "Sarajevo",
            "Split",
            "Ljubljana"
    );

    private String randomLocation() {
        return LOCATIONS.get((int) (Math.random() * LOCATIONS.size()));
    }

    private String randomTags() {
        Collections.shuffle(AVAILABLE_TAGS);
        return AVAILABLE_TAGS.stream()
                .limit(1 + (int) (Math.random() * 3)) // 1–3 taga
                .collect(Collectors.joining(","));
    } */

    private String randomTags() {
        List<String> tags = new ArrayList<>(List.of(
                "Music",
                "Vlog",
                "Travel",
                "Food",
                "Education",
                "Gaming",
                "Comedy",
                "Fitness"
        ));

        Collections.shuffle(tags);

        return String.join(",", tags.subList(0, 2));
    }

    private String randomLocation() {
        List<String> locations = List.of(
                "Beograd",
                "Novi Sad",
                "Niš",
                "Kragujevac",
                "Subotica",
                "Zagreb",
                "Sarajevo",
                "Split",
                "Ljubljana"
        );

        return locations.get(new Random().nextInt(locations.size()));
    }



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
        int videoCount = 100;
        Random random = new Random();

        for (int i = 1; i <= videoCount; i++) {

            VideoPost videoPost = VideoPost.builder()
                    .title("VideoPost broj " + i)
                    .description("Ovo je test videoPost broj " + i)
                    .tags(randomTags())
                    .thumbnailPath("uploads/thumbnails/" + (i%6+1) + ".jpg")
                    .videoPath("uploads/videos/" + (i%6+1) + ".mp4")
                    .createdAt(LocalDateTime.now())
                    .location(randomLocation())
                    .owner(owners.get(i % 2))
                    .likeCount(random.nextInt(10))
                    .commentCount(3)  //za pocetak 3 komentara
                    .viewCount(random.nextInt(100))
                    //.viewCount(0)
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
