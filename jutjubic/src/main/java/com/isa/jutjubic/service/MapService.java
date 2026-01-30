package com.isa.jutjubic.service;
import com.isa.jutjubic.dto.TileVideoDto;
import com.isa.jutjubic.dto.VideoPostDto;
import com.isa.jutjubic.dto.TileType;
import com.isa.jutjubic.model.MapTile;
import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.repository.MapTileRepository;
import com.isa.jutjubic.repository.VideoPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.isa.jutjubic.dto.TimeRange;

import static java.util.stream.Collectors.groupingBy;

@Service
public class MapService {

    @Autowired
    private VideoPostRepository videoPostRepository;

    @Autowired
    private MapTileRepository mapTileRepository;

    private static final int DETAIL_ZOOM = 14;

    // =====================================================
    // ENTRY POINT
    // =====================================================
    public List<TileVideoDto> getVideosForTiles(List<String> tileIds, TimeRange range) {

        int zoom = extractZoom(tileIds.get(0));

        if (zoom >= DETAIL_ZOOM) {
            return getDetailedVideos(tileIds, range);
        }

        return getAggregatedTiles(tileIds, range);
    }

    // =====================================================
    // AGGREGATE TILES (LOW / MID ZOOM)
    // =====================================================
    private List<TileVideoDto> getAggregatedTiles(List<String> tileIds, TimeRange range) {

        List<MapTile> tiles = mapTileRepository.findAllById(
                tileIds.stream()
                        .map(id -> id + "_" + range.name())
                        .toList()
        );

        return tiles.stream()
                .filter(t -> t.getVideoCount() > 0)
                .map(t -> {
                    String[] p = t.getId().split("_");

                    return new TileVideoDto(
                            t.getId(),
                            Integer.parseInt(p[0]),
                            Integer.parseInt(p[1]),
                            Integer.parseInt(p[2]),
                            t.getVideoCount(),
                            t.getCenterLat(),
                            t.getCenterLng(),
                            null,
                            TileType.AGGREGATE
                    );
                })
                .toList();
    }

    // =====================================================
    // DETAILED VIDEOS (HIGH ZOOM)
    // =====================================================
    private List<TileVideoDto> getDetailedVideos(List<String> tileIds, TimeRange range) {

        LocalDateTime from = resolveFrom(range);

        BoundingBox bbox = calculateBoundingBox(tileIds);

        List<VideoPost> videos = videoPostRepository.findForMap(
                from,
                bbox.minLat,
                bbox.maxLat,
                bbox.minLng,
                bbox.maxLng
        );

        Map<String, List<VideoPost>> grouped =
                videos.stream()
                        .collect(groupingBy(this::buildTileId));

        List<TileVideoDto> result = new ArrayList<>();

        grouped.forEach((tileId, vids) -> {
            if (!tileIds.contains(tileId)) return;

            String[] p = tileId.split("_");

            result.add(new TileVideoDto(
                    tileId,
                    Integer.parseInt(p[0]),
                    Integer.parseInt(p[1]),
                    Integer.parseInt(p[2]),
                    vids.size(),
                    0,
                    0,
                    vids.stream().map(this::mapToDto).toList(),
                    TileType.VIDEO
            ));
        });

        return result;
    }

    // =====================================================
    // EXISTING METHODS (NE DIRAMO)
    // =====================================================
    public LocalDateTime resolveFrom(TimeRange range) {
        return switch (range) {
            case LAST_30_DAYS -> LocalDateTime.now().minusDays(30);
            case THIS_YEAR -> LocalDateTime.now().withDayOfYear(1)
                    .withHour(0).withMinute(0).withSecond(0);
            case ALL -> LocalDateTime.of(2020, 1, 1, 0, 0);
        };
    }

    public VideoPostDto mapToDto(VideoPost post) {
        VideoPostDto dto = new VideoPostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setDescription(post.getDescription());
        dto.setTags(post.getTags());
        dto.setThumbnailPath(post.getThumbnailPath());
        dto.setVideoPath(post.getVideoPath());
        dto.setCreatedAt(post.getCreatedAt());
        if (post.getLocation() != null) {
            dto.setCity(post.getLocation().getCity());
            dto.setCountry(post.getLocation().getCountry());
            dto.setLatitude(post.getLocation().getLatitude());
            dto.setLongitude(post.getLocation().getLongitude());
        }
        dto.setOwnerUsername(post.getOwner().getUsername());
        dto.setLikeCount(post.getLikeCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setViewCount(post.getViewCount());
        dto.setOwnerId(post.getOwner().getId());
        return dto;
    }

    private int extractZoom(String tileId) {
        return Integer.parseInt(tileId.split("_")[0]);
    }

    private String buildTileId(VideoPost v) {
        int zoom = DETAIL_ZOOM;
        int x = lonToTileX(v.getLocation().getLongitude(), zoom);
        int y = latToTileY(v.getLocation().getLatitude(), zoom);
        return zoom + "_" + x + "_" + y;
    }

    private int lonToTileX(double lon, int zoom) {
        return (int) Math.floor((lon + 180) / 360 * (1 << zoom));
    }

    private int latToTileY(double lat, int zoom) {
        double latRad = Math.toRadians(lat);
        return (int) Math.floor(
                (1 - Math.log(Math.tan(latRad) + 1 / Math.cos(latRad)) / Math.PI)
                        / 2 * (1 << zoom)
        );
    }

    private BoundingBox calculateBoundingBox(List<String> tileIds) {

        double minLat = 90, maxLat = -90;
        double minLng = 180, maxLng = -180;

        for (String id : tileIds) {
            String[] p = id.split("_");
            int zoom = Integer.parseInt(p[0]);
            int x = Integer.parseInt(p[1]);
            int y = Integer.parseInt(p[2]);

            TileBounds b = tileToBounds(x, y, zoom);

            minLat = Math.min(minLat, b.minLat);
            maxLat = Math.max(maxLat, b.maxLat);
            minLng = Math.min(minLng, b.minLng);
            maxLng = Math.max(maxLng, b.maxLng);
        }

        return new BoundingBox(minLat, maxLat, minLng, maxLng);
    }

    private TileBounds tileToBounds(int x, int y, int zoom) {
        double n = Math.pow(2, zoom);

        double lon1 = x / n * 360.0 - 180.0;
        double lon2 = (x + 1) / n * 360.0 - 180.0;

        double lat1 = Math.toDegrees(Math.atan(Math.sinh(Math.PI * (1 - 2 * y / n))));
        double lat2 = Math.toDegrees(Math.atan(Math.sinh(Math.PI * (1 - 2 * (y + 1) / n))));

        return new TileBounds(lat2, lat1, lon1, lon2);
    }

    private record BoundingBox(
            double minLat,
            double maxLat,
            double minLng,
            double maxLng
    ) {}

    private record TileBounds(
            double minLat,
            double maxLat,
            double minLng,
            double maxLng
    ) {}
}
