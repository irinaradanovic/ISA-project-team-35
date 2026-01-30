package com.isa.jutjubic.service;

import com.isa.jutjubic.dto.TimeRange;
import com.isa.jutjubic.model.MapTile;
import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.repository.MapTileRepository;
import com.isa.jutjubic.repository.VideoPostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MapTileService {

    private final VideoPostRepository videoPostRepository;
    private final MapTileRepository mapTileRepository;

    // [S2] Nocno preracunavanje - svake noci u 03:00
    // cisti sve i racuna ispocetka na osnovu trenutno aktivnih videa
    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void rebuildAllTiles() {
        mapTileRepository.deleteAll(); // cistimo stare proracune

        // Hibernate će ovde povuci samo videe gde je deleted = false zbog @Where u modelu
        List<VideoPost> allVideos = videoPostRepository.findAll();
        for (VideoPost post : allVideos) {
            if (post.getLocation() != null) {
                processVideoInTiles(post);
            }
        }
        System.out.println("Sistem: Noćno preračunavanje mapa završeno.");
    }

    @Transactional
    public void updateTileForNewVideo(VideoPost post) {
        if (post.getLocation() == null) return;
        modifyTileCounts(post, 1); // Povecavamo brojac za 1
    }

    @Transactional
    public void updateTileForDeletedVideo(VideoPost post) {
        if (post.getLocation() == null) return;
        modifyTileCounts(post, -1); // Smanjujemo brojac za 1
    }

    // Pomoćna metoda koju koristi rebuildAllTiles
    private void processVideoInTiles(VideoPost post) {
        modifyTileCounts(post, 1);
    }

    // Glavna logika za promenu stanja markera na svim nivoima zumiranja
    private void modifyTileCounts(VideoPost post, int delta) {
        int[] zoomLevels = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18};
        List<MapTile> tilesToUpdate = new ArrayList<>();

        for (int zoom : zoomLevels) {
            int x = getTileX(post.getLocation().getLongitude(), zoom);
            int y = getTileY(post.getLocation().getLatitude(), zoom);

            for (TimeRange range : TimeRange.values()) {
                if (isWithinTimeRange(post.getCreatedAt(), range)) {
                    String tileId = String.format("%d_%d_%d_%s", zoom, x, y, range.name());

                    // Ako dodajemo video, kreiramo tile ako ne postoji.
                    // Ako brišemo, samo menjamo postojeći.
                    MapTile tile = mapTileRepository.findById(tileId)
                            .orElseGet(() -> delta > 0 ? new MapTile(
                                    tileId,
                                    zoom,
                                    0,
                                    LocalDateTime.now(),
                                    post.getLocation().getLatitude(),
                                    post.getLocation().getLongitude()
                            ) : null);

                    if (tile != null) {
                        tile.setVideoCount(Math.max(0, tile.getVideoCount() + delta));
                        tile.setLastUpdated(LocalDateTime.now());
                        tilesToUpdate.add(tile);
                    }
                }
            }
        }
        mapTileRepository.saveAll(tilesToUpdate);
    }

    private boolean isWithinTimeRange(LocalDateTime createdAt, TimeRange range) {
        if (createdAt == null) return false;
        LocalDateTime now = LocalDateTime.now();

        return switch (range) {
            case ALL -> true;
            case LAST_30_DAYS -> createdAt.isAfter(now.minusDays(30));
            case THIS_YEAR -> createdAt.getYear() == now.getYear();
        };
    }

    private int getTileX(double lon, int zoom) {
        return (int) Math.floor((lon + 180) / 360 * (1 << zoom));
    }

    private int getTileY(double lat, int zoom) {
        double latRad = Math.toRadians(lat);
        return (int) Math.floor((1 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI) / 2 * (1 << zoom));
    }
}