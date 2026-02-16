package com.isa.jutjubic.repository;

import com.isa.jutjubic.model.PopularVideo;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PopularVideoRepository extends JpaRepository<PopularVideo, Long> {

    // Svi popularni videi za određeni ETL run
    @EntityGraph(attributePaths = {"video", "video.owner"})
    List<PopularVideo> findByEtlRunIdOrderByRankPositionAsc(Long etlRunId);


    // Top 3 po ranku za određeni run
    @EntityGraph(attributePaths = {"video", "video.owner"})
    List<PopularVideo> findTop3ByEtlRunIdOrderByRankPositionAsc(Long etlRunId);


    // Brisanje po run-u (ako nekad želiš cleanup)
    void deleteByEtlRunId(Long etlRunId);
}
