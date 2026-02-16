package com.isa.jutjubic.service;

import com.isa.jutjubic.model.*;
import com.isa.jutjubic.repository.EtlRunRepository;
import com.isa.jutjubic.repository.PopularVideoRepository;
import com.isa.jutjubic.repository.VideoPostRepository;
import com.isa.jutjubic.repository.VideoViewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PopularVideoEtlService {

    private final VideoViewRepository videoViewRepository;
    private final VideoPostRepository videoPostRepository;
    private final EtlRunRepository etlRunRepository;
    private final PopularVideoRepository popularVideoRepository;

    /**
     * Pokreće se jednom dnevno u 02:00
     * Cron format: second minute hour day month day-of-week
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void runDailyEtl() {

        log.info("Starting daily Popular Video ETL job...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime sevenDaysAgo = now.minusDays(7);

        // 1️⃣ EXTRACT – agregirani pregledi po videu i po danu
        List<Object[]> rawData =
                videoViewRepository.countViewsGroupedByVideoAndDay(sevenDaysAgo);

        if (rawData.isEmpty()) {
            log.info("No video views found in last 7 days. ETL skipped.");
            return;
        }

        /*
         rawData struktura:
         [0] videoId (Integer)
         [1] date (java.sql.Date ili LocalDate)
         [2] count (Long)
        */

        // 2️⃣ TRANSFORM – računanje weighted popularity score-a
        Map<Integer, Double> popularityScoreMap = new HashMap<>();

        for (Object[] row : rawData) {

            Integer videoId = (Integer) row[0];

            // DATE može doći kao java.sql.Date
            LocalDate viewDate;
            if (row[1] instanceof java.sql.Date sqlDate) {
                viewDate = sqlDate.toLocalDate();
            } else {
                viewDate = (LocalDate) row[1];
            }

            Long dailyCount = (Long) row[2];

            long daysDifference = ChronoUnit.DAYS.between(viewDate, LocalDate.now());

            // weight = 7 - x + 1
            // ako je x=1 (jučerašnji), weight=7
            // ako je x=7, weight=1
            long weight = 7 - daysDifference;

            if (weight <= 0) continue;

            double weightedScore = dailyCount * weight;

            popularityScoreMap.merge(
                    videoId,
                    weightedScore,
                    Double::sum
            );
        }

        if (popularityScoreMap.isEmpty()) {
            log.info("No valid popularity scores calculated.");
            return;
        }

        // 3️⃣ Sortiranje po score DESC
        List<Map.Entry<Integer, Double>> sorted =
                popularityScoreMap.entrySet()
                        .stream()
                        .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                        .collect(Collectors.toList());

        // 4️⃣ Uzimamo TOP 3
        List<Map.Entry<Integer, Double>> top3 =
                sorted.stream().limit(3).toList();

        // 5️⃣ LOAD – kreiramo novi ETL run
        EtlRun etlRun = EtlRun.builder()
                .executedAt(LocalDateTime.now())
                .build();

        etlRunRepository.save(etlRun);

        int rank = 1;

        for (Map.Entry<Integer, Double> entry : top3) {

            Integer videoId = entry.getKey();
            Long score = entry.getValue().longValue();

            VideoPost video = videoPostRepository.findById(videoId)
                    .orElseThrow(() -> new RuntimeException("Video not found: " + videoId));

            PopularVideo popularVideo = PopularVideo.builder()
                    .video(video)
                    .etlRun(etlRun)
                    .popularityScore(score)
                    .rankPosition(rank++)
                    .build();

            popularVideoRepository.save(popularVideo);
        }

        log.info("Popular Video ETL completed successfully. Top {} videos stored.", top3.size());
    }
}
