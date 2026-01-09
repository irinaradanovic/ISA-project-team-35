package com.isa.jutjubic;

import com.isa.jutjubic.model.User;
import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.repository.UserRepository;
import com.isa.jutjubic.repository.VideoPostRepository;
import com.isa.jutjubic.service.VideoPostService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class VideoViewTest {
    @Autowired
    private VideoPostService service;

    @Autowired
    private VideoPostRepository repo;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testConcurrentViews() throws Exception {
        Date today = new Date();
        User user = new User();
        user.setUsername("testuser");
        user.setPasswordHash("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail("test@example.com");
        user.setAddress("123 Test St");
        user.setActive(true);
        user.setCreatedAt(today);

        userRepository.save(user);

        VideoPost video = new VideoPost();
        video.setOwner(user);
        video.setTitle("My video");
        video.setDescription("Test video");
        video.setCreatedAt(LocalDateTime.now());
        video.setViewCount(0);
        video.setLikeCount(0);
        video.setCommentCount(0);

        repo.save(video);

        Integer videoId = video.getId();

        int USERS = 50;

        ExecutorService executor = Executors.newFixedThreadPool(20);

        for(int i = 0; i < USERS; i++){
            executor.submit(() -> service.incrementViews(videoId));
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        Integer views = repo.findById(videoId).get().getViewCount();

        System.out.println("Broj pregleda = " + views);

        assertEquals(USERS, views);
    }
}
