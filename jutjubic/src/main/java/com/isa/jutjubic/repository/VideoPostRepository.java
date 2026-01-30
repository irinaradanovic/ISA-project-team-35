package com.isa.jutjubic.repository;

import com.isa.jutjubic.dto.VideoPostDto;
import com.isa.jutjubic.model.VideoPost;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VideoPostRepository extends JpaRepository<VideoPost, Integer> {

    @EntityGraph(attributePaths = {"owner"})
    Page<VideoPost> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("SELECT v.thumbnailPath FROM VideoPost v WHERE v.id = :id")
    Optional<String> findThumbnailPathById(@Param("id") Integer id);

    @Query("SELECT v FROM VideoPost v JOIN FETCH v.owner WHERE v.id = :id")
    Optional<VideoPost> findByIdWithOwner(@Param("id") Integer id);


    //3.7
    @Modifying
    @Transactional
    @Query("UPDATE VideoPost v SET v.viewCount = v.viewCount + 1 WHERE v.id = :id")
    int incrementViews(@Param("id") Integer id);

    @Query("SELECT v.viewCount FROM VideoPost v WHERE v.id = :id")
    Integer getViewCount(@Param("id") Integer id);

    @Query("SELECT v FROM VideoPost v JOIN FETCH v.owner WHERE v.owner.id = :id")
    List<VideoPost> findByOwnerId(@Param("id") Long id);


    //za mapu
    // OVO BI TREBALO DA VRACA SAMO VIDEE KOJI SE NALAZE IZMEDJU ODREDJENIH KOORDINATA, NISAM SIGURNA DA LI RADI SKROZ
    @Query("""
        SELECT v FROM VideoPost v
        JOIN FETCH v.owner
        WHERE v.location.latitude IS NOT NULL
        AND v.createdAt >= :from
        AND v.location.latitude BETWEEN :minLat AND :maxLat
        AND v.location.longitude BETWEEN :minLng AND :maxLng
        """)
    List<VideoPost> findForMap(
            @Param("from") LocalDateTime from,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng
    );

}
