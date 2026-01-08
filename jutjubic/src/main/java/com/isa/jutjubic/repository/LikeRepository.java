package com.isa.jutjubic.repository;

import com.isa.jutjubic.model.Comment;
import com.isa.jutjubic.model.Like;
import com.isa.jutjubic.model.User;
import com.isa.jutjubic.model.VideoPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndVideoPost(User user, VideoPost videoPost);
}
