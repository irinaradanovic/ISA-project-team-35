package com.isa.jutjubic.controller;

import com.isa.jutjubic.dto.TileRequest;
import com.isa.jutjubic.dto.TileVideoDto;
import com.isa.jutjubic.dto.TimeRange;
import com.isa.jutjubic.model.MapTile;
import com.isa.jutjubic.repository.MapTileRepository;
import com.isa.jutjubic.service.MapService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@CrossOrigin(origins = "http://localhost:5173") // front URL
@RequestMapping("/api/map")
@RequiredArgsConstructor
public class MapController {

    private final MapTileRepository mapTileRepository;
    @Autowired
    private MapService mapService;


    @PostMapping("/videos/tiles")
    public ResponseEntity<List<TileVideoDto>> getVideosForTiles(@RequestBody TileRequest request) {
        if (request.getTiles() == null || request.getTiles().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        List<TileVideoDto> videosByTile = mapService.getVideosForTiles(request.getTiles());
        return ResponseEntity.ok(videosByTile);
    }







   /* @GetMapping("/tiles")
    public ResponseEntity<?> getVisibleTiles(
            @RequestParam int zoom,
            @RequestParam List<Integer> xList,
            @RequestParam List<Integer> yList,
            @RequestParam(defaultValue = "ALL") TimeRange timeRange
    ) {
        if (xList.size() != yList.size()) {
            return ResponseEntity.badRequest().body("xList i yList moraju biti iste duzine");
        }

        List<String> tileIds = new ArrayList<>();
        for (int i = 0; i < xList.size(); i++) {
            tileIds.add(String.format("%d_%d_%d_%s", zoom, xList.get(i), yList.get(i), timeRange.name()));
        }

        List<MapTile> tiles = mapTileRepository.findAllById(tileIds);

        List<TileVideoDto> dtos = tiles.stream()
                .map(tile -> {
                    String[] parts = tile.getId().split("_");
                    int tZoom = Integer.parseInt(parts[0]);
                    int tX = Integer.parseInt(parts[1]);
                    int tY = Integer.parseInt(parts[2]);
                    return new TileVideoDto(tile.getId(), tZoom, tX, tY, tile.getVideoCount());
                })
                .toList();

        return ResponseEntity.ok(dtos);
    }*/







   /* @Autowired
    private MapService mapService;

    // Filtracija po vremenu
    // VEROVATNO TREBA DODATI ZOOM
    @GetMapping("/videos")
    public ResponseEntity<?> getVideosForMap(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLng,
            @RequestParam double maxLng,
            @RequestParam(defaultValue = "ALL") TimeRange timeRange
    ) {
        // Validacija
        if (minLat > maxLat || minLng > maxLng) {
            return ResponseEntity.badRequest().body("Invalid bounding box");
        }
        if (minLat < -90 || maxLat > 90 || minLng < -180 || maxLng > 180) {
            return ResponseEntity.badRequest().body("Invalid coordinates");
        }
        return ResponseEntity.ok(
                mapService.getVideosForMap(
                        minLat, maxLat, minLng, maxLng, timeRange
                )
        );
    }*/
}
