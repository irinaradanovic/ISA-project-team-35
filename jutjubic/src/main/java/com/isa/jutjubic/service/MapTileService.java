package com.isa.jutjubic.service;

import com.isa.jutjubic.dto.TimeRange;
import com.isa.jutjubic.model.MapTile;
import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.repository.MapTileRepository;
import com.isa.jutjubic.repository.VideoPostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MapTileService {

    @Autowired
    private VideoPostRepository videoPostRepository;
    @Autowired
    private MapTileRepository mapTileRepository;

    // [S2] nocno preracunavanje - svake noci u 03:00
    // masovno preracunavanje tilesova
    @Scheduled(cron = "0 0 3 * * ?")
    //@Scheduled(cron = "0 */1 * * * ?")  //za test, svaki minut
    @Transactional
    public void rebuildAllTiles() {
        mapTileRepository.deleteAll(); // cistimo stare proracune

        List<VideoPost> allVideos = videoPostRepository.findAll();
        for (VideoPost post : allVideos) {
            if (post.getLocation() != null ) {
                processVideoInTiles(post);
            }
        }
        System.out.println("Sistem: Noćno preračunavanje mapa završeno.");
    }

    // [S2] nakon uploada nove objave, azurirati taj tile
    @Transactional
    public void updateTileForNewVideo(VideoPost post) {
        if (post.getLocation() == null) return;
        processVideoInTiles(post);
    }

    private void processVideoInTiles(VideoPost post) {
        // Nivoi zumiranja koje aplikacija podrzava
        int[] zoomLevels = {2, 4, 6, 8, 10, 12, 15};

        for (int zoom : zoomLevels) {
            int x = getTileX(post.getLocation().getLongitude(), zoom);
            int y = getTileY(post.getLocation().getLatitude(), zoom);

            for (TimeRange range : TimeRange.values()) {
                // Provera da li video uopšte upada u ovaj vremenski filter
                if (isWithinTimeRange(post.getCreatedAt(), range)) {
                    String tileId = String.format("%d_%d_%d_%s", zoom, x, y, range.name());

                    MapTile tile = mapTileRepository.findById(tileId)
                            .orElse(new MapTile(
                                    tileId,
                                    zoom,
                                    0,
                                    LocalDateTime.now(),
                                    post.getLocation().getLatitude(),
                                    post.getLocation().getLongitude()
                            ));

                    tile.setVideoCount(tile.getVideoCount() + 1);
                    tile.setLastUpdated(LocalDateTime.now());
                    mapTileRepository.save(tile);
                }
            }
        }
    }

    // Pomoćna metoda za proveru vremenskog filtera
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
