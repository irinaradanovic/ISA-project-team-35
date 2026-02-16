package com.isa.jutjubic.repository;

import com.isa.jutjubic.model.VideoView;
import com.isa.jutjubic.model.VideoPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface VideoViewRepository extends JpaRepository<VideoView, Long> {

    // Svi pregledi za jedan video u poslednjih 7 dana
    @Query("""
        SELECT COUNT(v)
        FROM VideoView v
        WHERE v.video.id = :videoId
        AND v.viewedAt >= :from
    """)
    Long countViewsForVideoSince(
            @Param("videoId") Integer videoId,
            @Param("from") LocalDateTime from
    );


    // Grupisano po danu (za weighted score)
    @Query("""
        SELECT v.video.id, DATE(v.viewedAt), COUNT(v)
        FROM VideoView v
        WHERE v.viewedAt >= :from
        GROUP BY v.video.id, DATE(v.viewedAt)
    """)
    List<Object[]> countViewsGroupedByVideoAndDay(
            @Param("from") LocalDateTime from
    );


    // Svi videi koji imaju bar jedan pregled u poslednjih 7 dana
    @Query("""
        SELECT DISTINCT v.video
        FROM VideoView v
        WHERE v.viewedAt >= :from
    """)
    List<VideoPost> findVideosWithViewsSince(
            @Param("from") LocalDateTime from
    );


    // Ukupan broj pregleda po videu (za debugging ili statistiku)
    @Query("""
        SELECT v.video.id, COUNT(v)
        FROM VideoView v
        GROUP BY v.video.id
    """)
    List<Object[]> countAllViewsGroupedByVideo();
}
