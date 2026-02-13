package com.isa.jutjubic.controller;

import com.isa.jutjubic.dto.VideoPostDto;
import com.isa.jutjubic.dto.VideoPostUploadDto;
import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.repository.VideoPostRepository;
import com.isa.jutjubic.security.utils.SecurityUtils;
import com.isa.jutjubic.service.FileStorageService;
import com.isa.jutjubic.service.VideoPostService;
import jakarta.annotation.Resource;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/videoPosts")
@CrossOrigin(origins = "http://localhost:5173") // front URL
public class VideoPostController {
    @Autowired
    private VideoPostService postService;

    @Autowired
    private FileStorageService fileStorageService;
    @Autowired
    private VideoPostRepository videoPostRepository;

    @GetMapping("/{id}/play")
    public ResponseEntity<?> getVideoForPlayback(@PathVariable Integer id) {
        try {
            return ResponseEntity.ok(postService.getVideoForPlayback(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Video not found.");
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        }
    }

    @PostMapping("/{id}/end-stream")
    public ResponseEntity<Void> endStream(@PathVariable Integer id) {
        postService.finalizeStream(id);
        return ResponseEntity.ok().build();
    }



    @GetMapping("/{id}")
    public ResponseEntity<?> getPostById(@PathVariable Integer id) {
       try {
           VideoPostDto post = postService.getById(id);
           postService.incrementViews(id);
           return ResponseEntity.ok(post);
       } catch (NoSuchElementException e) {
           return ResponseEntity.status(404).body("Video post not found. Either deleted or unavailable");
       }
    }




    //u postmanu thumbnail i video postavljas sa racunara
    @PostMapping
    public ResponseEntity<?> createPost(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("tags") String tags,
            @RequestParam("video") MultipartFile video,
            @RequestParam("thumbnail") MultipartFile thumbnail,
            @RequestParam(required = false) String address,  // dodato
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude,

            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @RequestParam(required = false) LocalDateTime scheduledAt // za zakazani režim
    ) {

            VideoPostUploadDto dto = new VideoPostUploadDto();
            dto.setTitle(title);
            dto.setDescription(description);
            dto.setTags(tags);
            dto.setVideo(video);
            dto.setThumbnail(thumbnail);
            dto.setAddress(address);
            dto.setCountry(country);
            dto.setCity(city);
            dto.setLatitude(latitude);
            dto.setLongitude(longitude);
            dto.setScheduledAt(scheduledAt);

            try {
                VideoPostDto post = postService.createPost(dto);
                return ResponseEntity.ok(post);
            } catch (IOException e) {
                return ResponseEntity.badRequest().body("Upload failed: " + e.getMessage());
            }


    }

    @GetMapping("/{id}/thumbnail")
    @Transactional
    public ResponseEntity<byte[]> getThumbnail(@PathVariable Integer id) {
        byte[] image = postService.getThumbnail(id);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "image/jpeg")
                .body(image);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePost(@PathVariable Integer id) {
        try {
            postService.deletePost(id);
            return ResponseEntity.ok("VideoPost deleted successfully");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to delete video files: " + e.getMessage());
        }catch (SecurityException e){
            return ResponseEntity.status(403).body(e.getMessage());
        }

    }

    @GetMapping
    public Page<VideoPostDto> getAllVideos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        return postService.getLatestVideos(pageable);
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserVideos(@PathVariable Long userId){
       try {
           return ResponseEntity.ok(postService.getUsersVideos(userId));
       }catch(NoSuchElementException e){
           return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
       }
    }

    //VIDEI ZA MY PROFILE
    @GetMapping("/my")
    public ResponseEntity<List<VideoPostDto>> getMyVideos() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(postService.getUsersVideos(userId));
    }



}
