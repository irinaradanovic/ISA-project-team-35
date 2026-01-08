package com.isa.jutjubic.service;

import com.isa.jutjubic.model.Like;
import com.isa.jutjubic.model.User;
import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.repository.LikeRepository;
import com.isa.jutjubic.repository.UserRepository;
import com.isa.jutjubic.repository.VideoPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.Optional;

@Service
public class LikeService {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private VideoPostRepository videoPostRepository;

    @Autowired
    private UserRepository userRepository;

    // PRIVREMENO - fiksni korisnik dok nema login
    private User getCurrentUser() {
        return userRepository.findById(1L).orElseThrow();
    }

    @Transactional
    public int toggleLike(Integer videoId) {
        VideoPost video = videoPostRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        User user = getCurrentUser();

        Optional<Like> existing = likeRepository.findByUserAndVideoPost(user, video);

        if (existing.isPresent()) {
            // odlajkuj
            likeRepository.delete(existing.get());
            video.setLikeCount(video.getLikeCount() - 1);
        } else {
            // lajkuj
            Like like = Like.builder().user(user).videoPost(video).build();
            likeRepository.save(like);
            video.setLikeCount(video.getLikeCount() + 1);
        }

        videoPostRepository.save(video);
        return video.getLikeCount();
    }

    public boolean isLiked(Integer videoId) {
        VideoPost video = videoPostRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        User user = getCurrentUser();
        return likeRepository.findByUserAndVideoPost(user, video).isPresent();
    }
}
