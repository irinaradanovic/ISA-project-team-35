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

    //za zakazani režim
    @EntityGraph(attributePaths = {"owner"})
    @Query("""
    SELECT v FROM VideoPost v
    WHERE (v.scheduledAt IS NULL OR v.scheduledAt <= :now)
    ORDER BY v.createdAt DESC
""")
    Page<VideoPost> findVisibleVideos(@Param("now") LocalDateTime now, Pageable pageable);


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

    //za zakazani režim
    @Query("""
    SELECT v FROM VideoPost v
    JOIN FETCH v.owner
    WHERE (v.scheduledAt IS NULL OR v.scheduledAt <= :now)
    AND v.location.latitude IS NOT NULL
    AND v.createdAt >= :from
    AND v.location.latitude BETWEEN :minLat AND :maxLat
    AND v.location.longitude BETWEEN :minLng AND :maxLng
""")
    List<VideoPost> findVisibleForMap(
            @Param("now") LocalDateTime now,
            @Param("from") LocalDateTime from,
            @Param("minLat") double minLat,
            @Param("maxLat") double maxLat,
            @Param("minLng") double minLng,
            @Param("maxLng") double maxLng
    );

    @Query("SELECT v FROM VideoPost v JOIN FETCH v.owner WHERE v.id = :id")
    Optional<VideoPost> findByIdForPlayback(@Param("id") Integer id);



    @Modifying
    @Transactional
    @Query("UPDATE VideoPost v SET v.status = :status WHERE v.id = :id")
    int updateStatus(@Param("id") Integer id,
                     @Param("status") VideoPost.VideoStatus status);

    @Modifying
    @Transactional
    @Query("UPDATE VideoPost v SET v.videoPath = :path WHERE v.id = :id")
    int updateVideoPath(@Param("id") Integer id,
                        @Param("path") String path);

    @Modifying
    @Transactional
    @Query("UPDATE VideoPost v SET v.status = :newStatus WHERE v.id = :id AND v.status = :expected")
    int updateStatusIfCurrent(@Param("id") Integer id,
                              @Param("expected") VideoPost.VideoStatus expected,
                              @Param("newStatus") VideoPost.VideoStatus newStatus);
}
