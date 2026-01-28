package com.isa.jutjubic.config;

import com.isa.jutjubic.model.Comment;
import com.isa.jutjubic.model.GeoLocation;
import com.isa.jutjubic.model.User;
import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.repository.CommentRepository;
import com.isa.jutjubic.repository.UserRepository;
import com.isa.jutjubic.repository.VideoPostRepository;
import com.isa.jutjubic.service.MapTileService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final PasswordEncoder passwordEncoder;
    private final MapTileService mapTileService;

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

    private GeoLocation randomGeoLocation() {
        List<GeoLocation> locations = List.of(
                // Serbia
                new GeoLocation("Belgrade", "Serbia", 44.7866, 20.4489),
                new GeoLocation("Novi Sad", "Serbia", 45.2671, 19.8335),
                new GeoLocation("Niš", "Serbia", 43.3209, 21.8958),
                new GeoLocation("Kragujevac", "Serbia", 44.0128, 20.9114),
                new GeoLocation("Subotica", "Serbia", 46.1006, 19.6656),

                // Croatia
                new GeoLocation("Zagreb", "Croatia", 45.8150, 15.9819),
                new GeoLocation("Split", "Croatia", 43.5081, 16.4402),
                new GeoLocation("Rijeka", "Croatia", 45.3271, 14.4422),
                new GeoLocation("Osijek", "Croatia", 45.5540, 18.6955),

                // Bosnia and Herzegovina
                new GeoLocation("Sarajevo", "Bosnia and Herzegovina", 43.8563, 18.4131),
                new GeoLocation("Banja Luka", "Bosnia and Herzegovina", 44.7722, 17.1910),
                new GeoLocation("Mostar", "Bosnia and Herzegovina", 43.3438, 17.8078),

                // Montenegro
                new GeoLocation("Podgorica", "Montenegro", 42.4304, 19.2594),
                new GeoLocation("Nikšić", "Montenegro", 42.7731, 18.9445),

                // North Macedonia
                new GeoLocation("Skopje", "North Macedonia", 41.9981, 21.4254),
                new GeoLocation("Bitola", "North Macedonia", 41.0314, 21.3347),

                // Slovenia
                new GeoLocation("Ljubljana", "Slovenia", 46.0569, 14.5058),
                new GeoLocation("Maribor", "Slovenia", 46.5547, 15.6459),

                // Central Europe
                new GeoLocation("Budapest", "Hungary", 47.4979, 19.0402),
                new GeoLocation("Vienna", "Austria", 48.2082, 16.3738),
                new GeoLocation("Prague", "Czech Republic", 50.0755, 14.4378),
                new GeoLocation("Bratislava", "Slovakia", 48.1486, 17.1077),

                new GeoLocation("Valjevo", "Serbia", 44.2750, 19.8980),
                new GeoLocation("Brangović", "Serbia", 44.2201, 19.8704),


                new GeoLocation("Čačak", "Serbia", 43.8914, 20.3497),
                new GeoLocation("Užice", "Serbia", 43.8555, 19.8430),

                new GeoLocation("Sofia", "Bulgaria", 42.6977, 23.3219),
                new GeoLocation("Plovdiv", "Bulgaria", 42.1354, 24.7453),

                new GeoLocation("Bucharest", "Romania", 44.4268, 26.1025),
                new GeoLocation("Cluj-Napoca", "Romania", 46.7712, 23.6236),

                new GeoLocation("Munich", "Germany", 48.1351, 11.5820),
                new GeoLocation("Hamburg", "Germany", 53.5511, 9.9937),

                new GeoLocation("Milan", "Italy", 45.4642, 9.1900),
                new GeoLocation("Barcelona", "Spain", 41.3851, 2.1734),
                new GeoLocation("Paris", "France", 48.8566, 2.3522),
                new GeoLocation("Lyon", "France", 45.7640, 4.8357),

                new GeoLocation("Amsterdam", "Netherlands", 52.3676, 4.9041),
                new GeoLocation("Rotterdam", "Netherlands", 51.9244, 4.4777),

                new GeoLocation("Brussels", "Belgium", 50.8503, 4.3517),

                new GeoLocation("Copenhagen", "Denmark", 55.6761, 12.5683),

                new GeoLocation("Stockholm", "Sweden", 59.3293, 18.0686),
                new GeoLocation("Oslo", "Norway", 59.9139, 10.7522),

                new GeoLocation("Helsinki", "Finland", 60.1699, 24.9384),

                new GeoLocation("Warsaw", "Poland", 52.2297, 21.0122),
                new GeoLocation("Krakow", "Poland", 50.0647, 19.9450),

                new GeoLocation("Lisbon", "Portugal", 38.7223, -9.1393),

                new GeoLocation("Athens", "Greece", 37.9838, 23.7275),

                new GeoLocation("Zurich", "Switzerland", 47.3769, 8.5417)


        );

        return locations.get(new Random().nextInt(locations.size()));
    }


    private LocalDateTime randomCreatedAt() {
        Random random = new Random();
        double rand = random.nextDouble();

        int daysBack;
        if (rand < 0.30) {
            // 30% - poslednja 2 nedelje
            daysBack = random.nextInt(14);
        } else if (rand < 0.55) {
            // 25% - poslednji mesec (14-30 dana)
            daysBack = 14 + random.nextInt(17);
        } else if (rand < 0.75) {
            // 20% - poslednjih 3 meseca (31-90 dana)
            daysBack = 31 + random.nextInt(60);
        } else if (rand < 0.90) {
            // 15% - poslednjih 6 meseci (91-180 dana)
            daysBack = 91 + random.nextInt(90);
        } else {
            // 10% - stariji videi (181-730 dana)
            daysBack = 181 + random.nextInt(550);
        }

        int hours = random.nextInt(24);
        int minutes = random.nextInt(60);

        return LocalDateTime.now()
                .minusDays(daysBack)
                .minusHours(hours)
                .minusMinutes(minutes);
    }


    @Override
    @Transactional
    public void run(String... args) {

        // Provera: Ako već imamo korisnike, nemoj raditi ništa
        if (userRepository.count() > 0) {
            System.out.println("Podaci već postoje u bazi, preskačem učitavanje.");
            return;
        }

        User user1 = User.builder()
                .email("marko@test.com")
                .username("marko")
                .passwordHash(passwordEncoder.encode("password"))
                .firstName("Marko")
                .lastName("Markovic")
                .address("Novi Sad")
                .active(true)
                .build();

        User user2 = User.builder()
                .email("ana@test.com")
                .username("ana")
                .passwordHash(passwordEncoder.encode("password"))
                .firstName("Ana")
                .lastName("Anic")
                .address("Beograd")
                .active(true)
                .build();

        User user3 = User.builder()
                .email("miki@test.com")
                .username("miki")
                .passwordHash(passwordEncoder.encode("password"))
                .firstName("Miki")
                .lastName("Mikic")
                .address("Adresa")
                .active(true)
                .build();

        User user4 = User.builder()
                .email("jelena@test.com")
                .username("jelena123")
                .passwordHash(passwordEncoder.encode("password"))
                .firstName("Jelena")
                .lastName("Jelic")
                .address("Adresa1")
                .active(true)
                .build();

        User user5 = User.builder()
                .email("milica@test.com")
                .username("milica20")
                .passwordHash(passwordEncoder.encode("password"))
                .firstName("Milica")
                .lastName("Milic")
                .address("Adresa 2")
                .active(true)
                .build();

        userRepository.saveAll(List.of(user1, user2 , user3, user4, user5));

        List<User> owners = List.of(user1, user2, user3, user4, user5);

        int videoCount = 5000;
        Random random = new Random();

        /*for (int i = 1; i <= videoCount; i++) {

            VideoPost videoPost = VideoPost.builder()
                    .title("VideoPost broj " + i)
                    .description("Ovo je test videoPost broj " + i)
                    .tags(randomTags())
                    .thumbnailPath("uploads/thumbnails/" + (i%6+1) + ".jpg")
                    .videoPath("uploads/videos/" + (i%6+1) + ".mp4")
                    .createdAt(randomCreatedAt())
                    .location(randomGeoLocation())
                    .owner(owners.get(i%5))
                    .likeCount(random.nextInt(10))
                    .commentCount(3)  //za pocetak 3 komentara
                    .viewCount(random.nextInt(100))
                    //.viewCount(0)
                    .build();

            videoPostRepository.save(videoPost);

            for (int j = 1; j <= 15; j++) {
                Comment comment = Comment.builder()
                        .content("Komentar " + j + " na VideoPost " + i)
                        .author(owners.get((i + j) % 5))
                        .videoPost(videoPost)
                        .build();
                commentRepository.save(comment);
            }

        }*/
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

            videoBatch.add(video);

            for (int j = 1; j <= 3; j++) {
                Comment comment = Comment.builder()
                        .content("Komentar " + j + " na video " + i)
                        .author(owners.get((i + j) % owners.size()))
                        .videoPost(video)
                        .build();
                commentBatch.add(comment);
            }

            // Batch save na svakih 500 videa
            if (i % 500 == 0) {
                videoPostRepository.saveAll(videoBatch);
                commentRepository.saveAll(commentBatch);
                videoBatch.clear();
                commentBatch.clear();
                System.out.println(i + " videa ubaceno...");
            }
        }

        // ostatak
        if (!videoBatch.isEmpty()) {
            videoPostRepository.saveAll(videoBatch);
            commentRepository.saveAll(commentBatch);
        }

        System.out.println("Svi test video postovi ubaceni.");

        //Rebuild tile-ova JEDNOM
        mapTileService.rebuildAllTiles();
        System.out.println("Map tiles rebuilt for test data.");
    }
}
