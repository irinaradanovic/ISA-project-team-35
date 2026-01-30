package com.isa.jutjubic.service;
import com.isa.jutjubic.dto.TileVideoDto;
import com.isa.jutjubic.dto.VideoPostDto;
import com.isa.jutjubic.model.MapTile;
import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.repository.MapTileRepository;
import com.isa.jutjubic.repository.VideoPostRepository;
import com.isa.jutjubic.security.utils.TileBounds;
import com.isa.jutjubic.security.utils.TileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import com.isa.jutjubic.dto.TimeRange;

@Service
public class MapService {
    @Autowired
    private VideoPostRepository videoPostRepository;

    @Autowired
    private MapTileRepository mapTileRepository;


    public List<TileVideoDto> getVideosForTiles(List<String> tileIds, String timeRangeStr) {
        TimeRange range = TimeRange.valueOf(timeRangeStr);
        List<String> dbIds = tileIds.stream().map(id -> id + "_" + range.name()).toList();

        // Povuci SVE kesirane podatke odjednom (Batch)
        List<MapTile> cachedTiles = mapTileRepository.findAllById(dbIds);
        List<TileVideoDto> result = new ArrayList<>();
        LocalDateTime fromDate = resolveFrom(range);

        for (String tileId : tileIds) {
            // S2 OPTIMIZACIJA: Proveravamo pre-izračunati MapTile
            // tileId koji front šalje je npr "5_18_12", ali u bazi je "5_18_12_ALL"
            String dbTileId = tileId + "_" + range.name();

            var currentTile = cachedTiles.stream()
                    .filter(t -> t.getId().equals(dbTileId))
                    .findFirst();

            // Ako tile NE POSTOJI u bazi (isEmpty) ili je videoCount 0,
            // to znaci da tamo nema videa. PRESKOCI UPIT KA BAZI.
            if (currentTile.isEmpty() || currentTile.get().getVideoCount() == 0) {
                result.add(new TileVideoDto(tileId, new ArrayList<>()));
                continue;
            }

            //  Samo ako kes potvrdi da videa IMA, radimo fetch
            TileBounds bounds = TileUtils.tileToBounds(tileId);
            List<VideoPostDto> videos = videoPostRepository
                    .findForMap(fromDate, bounds.getMinLat(), bounds.getMaxLat(),
                            bounds.getMinLng(), bounds.getMaxLng())
                    .stream().map(this::mapToDto).toList();

            result.add(new TileVideoDto(tileId, videos));
        }
        return result;
    }


    public LocalDateTime resolveFrom(TimeRange range) {
        return switch (range) {
            case LAST_30_DAYS -> LocalDateTime.now().minusDays(30);
            case THIS_YEAR -> LocalDateTime.now().withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
            case ALL -> LocalDateTime.of(2020, 1, 1, 0, 0); // all svi videi od 2020. npr
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
}
