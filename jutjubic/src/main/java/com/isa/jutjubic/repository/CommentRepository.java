package com.isa.jutjubic.repository;

import com.isa.jutjubic.model.Comment;
import com.isa.jutjubic.model.VideoPost;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByVideoPostOrderByCreatedAtDesc(VideoPost videoPost, Pageable pageable);
    List<Comment> findByVideoPost(VideoPost videoPost);

    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.author " +
            "WHERE c.videoPost.id = :videoId " +
            "ORDER BY c.createdAt DESC")
    List<Comment> findAllByVideoId(@Param("videoId") Integer videoId);

}
