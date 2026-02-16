package com.isa.jutjubic.controller;

import com.isa.jutjubic.dto.PopularVideoDto;
import com.isa.jutjubic.model.EtlRun;
import com.isa.jutjubic.model.PopularVideo;
import com.isa.jutjubic.repository.EtlRunRepository;
import com.isa.jutjubic.repository.PopularVideoRepository;
import com.isa.jutjubic.service.PopularVideoEtlService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/popular-videos")
@RequiredArgsConstructor
public class PopularVideoController {

    private final EtlRunRepository etlRunRepository;
    private final PopularVideoRepository popularVideoRepository;
    private final PopularVideoEtlService popularVideoEtlService;

    /**
     * Vraća TOP 3 videa iz poslednjeg ETL run-a
     */
    @GetMapping
    public ResponseEntity<List<PopularVideoDto>> getLatestPopularVideos() {

        // 1️⃣ Pronađi poslednji ETL run
        EtlRun latestRun = etlRunRepository.findTopByOrderByExecutedAtDesc()
                .orElse(null);

        if (latestRun == null) {
            return ResponseEntity.ok(List.of());
        }

        // 2️⃣ Uzmi sve popular videe za taj run, sortirane po rank-u
        List<PopularVideo> popularVideos =
                popularVideoRepository.findByEtlRunIdOrderByRankPositionAsc(latestRun.getId());


        // 3️⃣ Mapiranje u DTO
        List<PopularVideoDto> result = popularVideos.stream()
                .map(pv -> PopularVideoDto.builder()
                        .videoId(pv.getVideo().getId())
                        .title(pv.getVideo().getTitle())
                        .thumbnailPath(pv.getVideo().getThumbnailPath())
                        .popularityScore(pv.getPopularityScore())
                        .rankPosition(pv.getRankPosition())
                        .build()
                )
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }

    /**
     * MANUAL endpoint za pokretanje ETL pipeline-a
     */
    @PostMapping("/run")
    public ResponseEntity<String> runEtlManually() {
        popularVideoEtlService.runDailyEtl(); // poziv metode koja pravi ETL
        return ResponseEntity.ok("ETL pipeline executed successfully.");
    }


}
