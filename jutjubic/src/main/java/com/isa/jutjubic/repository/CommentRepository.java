package com.isa.jutjubic.repository;

import com.isa.jutjubic.model.Comment;
import com.isa.jutjubic.model.VideoPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByVideoPostOrderByCreatedAtDesc(VideoPost videoPost, Pageable pageable);
    List<Comment> findByVideoPost(VideoPost videoPost);

}
