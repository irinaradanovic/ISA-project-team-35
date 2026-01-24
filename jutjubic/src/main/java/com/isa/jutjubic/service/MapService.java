package com.isa.jutjubic.service;
import com.isa.jutjubic.dto.VideoPostDto;
import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.repository.VideoPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import com.isa.jutjubic.dto.TimeRange;

@Service
public class MapService {
    @Autowired
    private VideoPostRepository videoPostRepository;

    public LocalDateTime resolveFrom(TimeRange range) {
        return switch (range) {
            case LAST_30_DAYS -> LocalDateTime.now().minusDays(30);
            case THIS_YEAR -> LocalDateTime.now().withDayOfYear(1).withHour(0).withMinute(0).withSecond(0);
            case ALL -> LocalDateTime.of(2020, 1, 1, 0, 0); // all svi videi od 2020. npr
        };
    }

    public List<VideoPostDto> getVideosForMap(
            double minLat,
            double maxLat,
            double minLng,
            double maxLng,
            TimeRange timeRange
    ) {
        LocalDateTime from = resolveFrom(timeRange);

        return videoPostRepository
                .findForMap(from, minLat, maxLat, minLng, maxLng)
                .stream()
                .map(this::mapToDto)
                .toList();
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
