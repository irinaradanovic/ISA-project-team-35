package com.isa.jutjubic.repository;

import com.isa.jutjubic.model.VideoPost;
import jakarta.persistence.criteria.CriteriaBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoPostRepository extends JpaRepository<VideoPost, Integer> {


    Page<VideoPost> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
