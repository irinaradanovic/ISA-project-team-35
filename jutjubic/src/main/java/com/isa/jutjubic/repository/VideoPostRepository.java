package com.isa.jutjubic.repository;

import com.isa.jutjubic.model.VideoPost;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface VideoPostRepository extends JpaRepository<VideoPost, Integer> {

    @EntityGraph(attributePaths = {"owner"})
    Page<VideoPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT v.thumbnailPath FROM VideoPost v WHERE v.id = :id")
    Optional<String> findThumbnailPathById(@Param("id") Integer id);
}
