package com.isa.jutjubic.repository;

import com.isa.jutjubic.model.EtlRun;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EtlRunRepository extends JpaRepository<EtlRun, Long> {

    // Poslednje pokretanje ETL-a
    Optional<EtlRun> findTopByOrderByExecutedAtDesc();


    // Poslednje pokretanje sa popularnim videima (za homepage)
    @EntityGraph(attributePaths = {"popularVideos", "popularVideos.video"})
    Optional<EtlRun> findTopByOrderByExecutedAtDescIdDesc();
}
