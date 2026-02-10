package com.isa.jutjubic.service;

import com.isa.jutjubic.dto.VideoPostDto;
import com.isa.jutjubic.dto.VideoPostUploadDto;
import com.isa.jutjubic.model.GeoLocation;
import com.isa.jutjubic.model.User;
import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.repository.UserRepository;
import com.isa.jutjubic.repository.VideoPostRepository;
import com.isa.jutjubic.security.utils.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.ServiceUnavailableException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
public class VideoPostService {

    @Autowired
    private VideoPostRepository postRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private MapTileService mapTileService;

    @Autowired
    private GeocodingService geocodingService;

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
        dto.setStreaming(post.isStreaming());
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public VideoPostDto createPost(VideoPostUploadDto dto) throws IOException {
        if (dto.getVideo().getSize() > 200 * 1024 * 1024) {
            throw new IOException("Video file too large (max 200MB)");
        }
        if (!dto.getVideo().getContentType().equals("video/mp4")) {
            throw new IllegalArgumentException("Only MP4 videos are allowed");
        }
        if (dto.getThumbnail() == null || dto.getThumbnail().isEmpty()) {
            throw new IllegalArgumentException("Thumbnail is required");
        }
        if (dto.getTags() == null || dto.getTags().isEmpty()) {
            throw new IllegalArgumentException("At least one tag is required");
        }

        long startTime = System.currentTimeMillis();
        long timeoutMillis = 10_000; // 10 sekundi

        String videoPath = null;
        String thumbnailPath = null;

        try {
            thumbnailPath = fileStorageService.saveFile(dto.getThumbnail(), "thumbnails");

            // proveri da li je proslo više od timeout-a
            if (System.currentTimeMillis() - startTime > timeoutMillis) {
                throw new IOException("Upload timeout - thumbnail upload took too long");
            }

            videoPath = fileStorageService.saveFile(dto.getVideo(), "videos");

            // proveri ponovo
            if (System.currentTimeMillis() - startTime > timeoutMillis) {
                throw new IOException("Upload timeout - video upload took too long");
            }

            Long ownerId = SecurityUtils.getCurrentUserId();  //pronalazimo ulogovanog korisnika
            User owner = userRepository.findById(ownerId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            VideoPost post = new VideoPost();
            post.setTitle(dto.getTitle());
            post.setDescription(dto.getDescription());
            post.setTags(dto.getTags());
            post.setThumbnailPath(thumbnailPath);
            post.setVideoPath(videoPath);
            post.setCreatedAt(LocalDateTime.now());
            post.setOwner(owner);
            GeoLocation loc = new GeoLocation();

                 // Ako frontend NIJE poslao koordinate, tek onda zovi API (fallback)
            if (dto.getLatitude() == null || dto.getLongitude() == null) {
                if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
                    geocodingService.fillLocationData(dto, loc);
                }
            } else {
                // Ako je frontend već odradio posao, samo prepisi vrednosti
                loc.setCity(dto.getCity());
                loc.setCountry(dto.getCountry());
                loc.setLatitude(dto.getLatitude());
                loc.setLongitude(dto.getLongitude());
                loc.setAddress(dto.getAddress());
            }

            post.setLocation(loc);


            postRepository.save(post);

            // [S2] dodato za mapu 3.18:
            if (post.getLocation() != null) {
                mapTileService.updateTileForNewVideo(post);
            }

            return mapToDto(post);

        } catch (IOException e) {
            // rollback fajlova ako upload ne uspe
            if (thumbnailPath != null) fileStorageService.deleteFile(thumbnailPath);
            if (videoPath != null) fileStorageService.deleteFile(videoPath);
            throw e;
        }
    }

    @Transactional
    public void deletePost(Integer id) throws IOException {

        Long userId = SecurityUtils.getCurrentUserId();

        VideoPost post = postRepository.findByIdWithOwner(id)
                .orElseThrow(() -> new NoSuchElementException("VideoPost not found with id " + id));

        if(!post.getOwner().getId().equals(userId)){
            throw new SecurityException("You cannot delete someone else's video!");
        }

        // dodato: Smanjujemo brojace na mapi pre nego što obelezimo video kao obrisan
        if (post.getLocation() != null) {
            mapTileService.updateTileForDeletedVideo(post);
        }
        post.setDeleted(true);

        if (post.getThumbnailPath() != null) {
            cacheManager.getCache("thumbnails")
                    .evict(post.getThumbnailPath());
        }

        // 1. obrisi fajlove sa diska
      /*  if (post.getThumbnailPath() != null) {
            fileStorageService.deleteFile(post.getThumbnailPath()); // ovo uklanja i iz keša
        }
        if (post.getVideoPath() != null) {
            Path videoPath = Paths.get(post.getVideoPath());
            if (Files.exists(videoPath)) {
                Files.delete(videoPath);
            }
        }  */

        // 2. obrisi iz baze
        //postRepository.delete(post);
    }

    @Retryable(retryFor = { DataAccessResourceFailureException.class }, maxAttempts = 2, backoff = @Backoff(delay = 3000)) //dodato za 3.11
    public VideoPostDto getById(Integer Id){
        VideoPost post =  postRepository.findByIdWithOwner(Id).orElseThrow(() -> new NoSuchElementException("VideoPost not found with id " + Id));
        return mapToDto(post);
    }

    public byte[] getThumbnail(Integer videoId) {
        String path = postRepository.findThumbnailPathById(videoId)
                .orElseThrow(() -> new NoSuchElementException("Thumbnail path not found for id " + videoId));
        return fileStorageService.loadThumbnail(path);   //KESIRANJE IZVRSENO FILE STORAGE SERVICE
    }

    @Retryable(retryFor = { DataAccessResourceFailureException.class }, maxAttempts = 2, backoff = @Backoff(delay = 3000))
    @Transactional(readOnly = true)
    public Page<VideoPostDto> getLatestVideos(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToDto);
    }

    //dodato za 3.11
    @Retryable(
            retryFor = { DataAccessResourceFailureException.class },  //greska pri citanju baze
            maxAttempts = 2,                                          //max broj pokusaja
            backoff = @Backoff(delay = 3000)                          //sacekaj 3 skeunde
    )
    public void incrementViews(Integer videoId) {
        int updated = postRepository.incrementViews(videoId);
        if (updated == 0) {
            throw new NoSuchElementException("Video not found with id: " + videoId);
        }
    }

    public List<VideoPostDto> getUsersVideos(Long userId){
        List<VideoPost> videos = postRepository.findByOwnerId(userId);

        if(videos.isEmpty()){
            throw new NoSuchElementException("Can't find any videos for user id: " + userId);
        }
        videos.sort(Comparator.comparing(VideoPost::getCreatedAt).reversed());

        return videos.stream().map(this::mapToDto).toList();
    }

    //ako je proslo 2 pokusaja, 6 sekundi
    @Recover
    public VideoPostDto recoverGetById(DataAccessResourceFailureException e, Integer id) {
        System.out.println("[RECOVER] getById failed for id=" + id + ": " + e.getMessage());
        throw new RuntimeException("Baza podataka je trenutno nedostupna.");
    }

    @Recover
    public void recoverIncrementViews(DataAccessResourceFailureException e, Integer videoId) {
        System.out.println("[RECOVER] incrementViews failed for videoId=" + videoId + " - preskačemo");
    }
}
