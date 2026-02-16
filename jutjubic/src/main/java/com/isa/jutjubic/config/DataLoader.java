package com.isa.jutjubic.config;

import com.isa.jutjubic.model.Comment;
import com.isa.jutjubic.model.GeoLocation;
import com.isa.jutjubic.model.User;
import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.model.VideoView;
import com.isa.jutjubic.repository.CommentRepository;
import com.isa.jutjubic.repository.UserRepository;
import com.isa.jutjubic.repository.VideoPostRepository;
import com.isa.jutjubic.repository.VideoViewRepository;
import com.isa.jutjubic.service.MapTileService;
import com.isa.jutjubic.service.TranscodingProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

@Component
@Profile("local")
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VideoPostRepository videoPostRepository;
    private final CommentRepository commentRepository;
    private final VideoViewRepository videoViewRepository;
    private final PasswordEncoder passwordEncoder;
    private final MapTileService mapTileService;
    private final TranscodingProducer transcodingProducer;

    private String randomTags() {
        List<String> tags = new ArrayList<>(List.of(
                "Music", "Vlog", "Travel", "Food", "Education", "Gaming", "Comedy", "Fitness"
        ));
        Collections.shuffle(tags);
        return String.join(",", tags.subList(0, 2));
    }

    private GeoLocation randomGeoLocation() {
        List<GeoLocation> hotspots = List.of(
                new GeoLocation("Knez Mihailova, Belgrade", "Belgrade", "Serbia", 44.8150, 20.4612),
                new GeoLocation("Kalemegdan Park, Belgrade", "Belgrade", "Serbia", 44.8228, 20.4503),
                new GeoLocation("SPENS, Novi Sad", "Novi Sad", "Serbia", 45.2442, 19.8431),
                new GeoLocation("Štrand, Novi Sad", "Novi Sad", "Serbia", 45.2346, 19.8458),
                new GeoLocation("Baščaršija, Sarajevo", "Sarajevo", "BiH", 43.8598, 18.4313),
                new GeoLocation("Riva, Split", "Split", "Croatia", 43.5075, 16.4392),
                new GeoLocation("Trg Bana Jelačića, Zagreb", "Zagreb", "Croatia", 45.8131, 15.9772),
                new GeoLocation("Skopje Fortress, Skopje", "Skopje", "N. Macedonia", 42.0007, 21.4335),
                new GeoLocation("Stephansplatz, Vienna", "Vienna", "Austria", 48.2082, 16.3731),
                new GeoLocation("Charles Bridge, Prague", "Prague", "Czech Republic", 50.0865, 14.4114),
                new GeoLocation("Brandenburg Gate, Berlin", "Berlin", "Germany", 52.5163, 13.3777),
                new GeoLocation("Eiffel Tower, Paris", "Paris", "France", 48.8584, 2.2945),
                new GeoLocation("Colosseum, Rome", "Rome", "Italy", 41.8902, 12.4922),
                new GeoLocation("Sagrada Familia, Barcelona", "Barcelona", "Spain", 41.4036, 2.1744),
                new GeoLocation("Dam Square, Amsterdam", "Amsterdam", "Netherlands", 52.3731, 4.8926)
        );

        GeoLocation base = hotspots.get(new Random().nextInt(hotspots.size()));
        Random r = new Random();
        double jitterLat = (r.nextDouble() - 0.5) * 0.003;
        double jitterLng = (r.nextDouble() - 0.5) * 0.003;

        return new GeoLocation(
                base.getAddress(),
                base.getCity(),
                base.getCountry(),
                base.getLatitude() + jitterLat,
                base.getLongitude() + jitterLng
        );
    }

    private LocalDateTime randomCreatedAt() {
        Random random = new Random();
        double rand = random.nextDouble();
        int daysBack;
        if (rand < 0.30) daysBack = random.nextInt(14);
        else if (rand < 0.55) daysBack = 14 + random.nextInt(17);
        else if (rand < 0.75) daysBack = 31 + random.nextInt(60);
        else if (rand < 0.90) daysBack = 91 + random.nextInt(90);
        else daysBack = 181 + random.nextInt(550);

        int hours = random.nextInt(24);
        int minutes = random.nextInt(60);

        return LocalDateTime.now()
                .minusDays(daysBack)
                .minusHours(hours)
                .minusMinutes(minutes);
    }

    @Override
    public void run(String... args) {

        if (userRepository.count() > 0) {
            System.out.println("Podaci već postoje u bazi, preskačem učitavanje.");
            return;
        }

        // --- USER ---
        User user1 = User.builder().email("marko@test.com").username("marko")
                .passwordHash(passwordEncoder.encode("password")).firstName("Marko")
                .lastName("Markovic").address("Novi Sad").active(true).build();
        User user2 = User.builder().email("ana@test.com").username("ana")
                .passwordHash(passwordEncoder.encode("password")).firstName("Ana")
                .lastName("Anic").address("Beograd").active(true).build();
        User user3 = User.builder().email("miki@test.com").username("miki")
                .passwordHash(passwordEncoder.encode("password")).firstName("Miki")
                .lastName("Mikic").address("Adresa").active(true).build();
        User user4 = User.builder().email("jelena@test.com").username("jelena123")
                .passwordHash(passwordEncoder.encode("password")).firstName("Jelena")
                .lastName("Jelic").address("Adresa1").active(true).build();
        User user5 = User.builder().email("milica@test.com").username("milica20")
                .passwordHash(passwordEncoder.encode("password")).firstName("Milica")
                .lastName("Milic").address("Adresa 2").active(true).build();

        userRepository.saveAll(List.of(user1, user2, user3, user4, user5));
        List<User> owners = List.of(user1, user2, user3, user4, user5);

        // --- VIDEO POST + COMMENT ---
        int videoCount = 5000;
        Random random = new Random();
        List<VideoPost> videoBatch = new ArrayList<>();
        List<Comment> commentBatch = new ArrayList<>();

        for (int i = 1; i <= videoCount; i++) {
            GeoLocation loc = randomGeoLocation();
            User owner = owners.get(i % owners.size());

            VideoPost video = VideoPost.builder()
                    .title("Video " + i)
                    .description("Test video " + i)
                    .tags(randomTags())
                    .thumbnailPath("uploads/thumbnails/" + (i % 6 + 1) + ".jpg")
                    .videoPath("uploads/videos/" + (i % 6 + 1) + ".mp4")
                    .createdAt(randomCreatedAt())
                    .location(loc)
                    .owner(owner)
                    .likeCount(random.nextInt(100))
                    .commentCount(3)
                    .viewCount(random.nextInt(1000))
                    .build();
            video.setStatus(VideoPost.VideoStatus.PENDING);
            videoBatch.add(video);

            for (int j = 1; j <= 3; j++) {
                Comment comment = Comment.builder()
                        .content("Komentar " + j + " na video " + i)
                        .author(owners.get((i + j) % owners.size()))
                        .videoPost(video)
                        .build();
                commentBatch.add(comment);
            }

            if (i % 500 == 0) {
                videoPostRepository.saveAllAndFlush(videoBatch);
                commentRepository.saveAll(commentBatch);
                videoBatch.clear();
                commentBatch.clear();
                System.out.println(i + " videa i komentara ubaceno...");
            }
        }

        if (!videoBatch.isEmpty()) {
            videoPostRepository.saveAllAndFlush(videoBatch);
            commentRepository.saveAll(commentBatch);
        }

        System.out.println("Svi test video postovi i komentari ubačeni.");

        mapTileService.rebuildAllTiles();
        System.out.println("Map tiles rebuilt for test data.");

        // --- VIDEO VIEW ---
        List<VideoPost> allVideos = videoPostRepository.findAll();
        List<User> allUsers = userRepository.findAll();
        List<VideoView> viewBatch = new ArrayList<>();
        int totalViews = 5000;

        for (int i = 0; i < totalViews; i++) {
            VideoPost video = allVideos.get(random.nextInt(allVideos.size()));
            User user = random.nextDouble() < 0.8 ? allUsers.get(random.nextInt(allUsers.size())) : null;
            LocalDateTime viewedAt = LocalDateTime.now()
                    .minusDays(random.nextInt(7))
                    .minusHours(random.nextInt(24))
                    .minusMinutes(random.nextInt(60));

            VideoView view = VideoView.builder()
                    .video(video)
                    .user(user)
                    .viewedAt(viewedAt)
                    .build();
            viewBatch.add(view);

            if (viewBatch.size() % 500 == 0) {
                videoViewRepository.saveAll(viewBatch);
                viewBatch.clear();
                System.out.println(i + " video view-ova ubačeno...");
            }
        }

        if (!viewBatch.isEmpty()) {
            videoViewRepository.saveAll(viewBatch);
        }

        System.out.println("Svi testni video pregledi ubačeni u bazu.");
    }
}
