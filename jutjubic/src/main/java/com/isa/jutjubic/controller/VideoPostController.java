package com.isa.jutjubic.controller;

import com.isa.jutjubic.dto.VideoPostDto;
import com.isa.jutjubic.dto.VideoPostUploadDto;
import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.repository.VideoPostRepository;
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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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


   @GetMapping("/{id}")
   //@Transactional
    public ResponseEntity<VideoPostDto> getPostById(@PathVariable Integer id) {
        VideoPostDto post = postService.getById(id);
        postService.incrementViews(id);
        return ResponseEntity.ok(post);
    }

   /* @PostMapping("/{id}/view")
    public ResponseEntity<Void> incrementView(@PathVariable Integer id) {
        postService.incrementView(id);
        return ResponseEntity.ok().build();
    } */


    private VideoPostDto mapToDto(VideoPost post) {
        VideoPostDto dto = new VideoPostDto();
        dto.setId(post.getId());
        dto.setTitle(post.getTitle());
        dto.setDescription(post.getDescription());
        dto.setTags(post.getTags());
        dto.setThumbnailPath(post.getThumbnailPath());
        dto.setVideoPath(post.getVideoPath());
        dto.setCreatedAt(post.getCreatedAt());
        dto.setLocation(post.getLocation());
        return dto;
    }


    //u postmanu thumbnail i video postavljas sa racunara
    @PostMapping
    public ResponseEntity<?> createPost(
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("tags") String tags,
            @RequestParam("video") MultipartFile video,
            @RequestParam("thumbnail") MultipartFile thumbnail,
            @RequestParam(value = "location", required = false) String location
    ) {

            VideoPostUploadDto dto = new VideoPostUploadDto();
            dto.setTitle(title);
            dto.setDescription(description);
            dto.setTags(tags);
            dto.setVideo(video);
            dto.setThumbnail(thumbnail);
            dto.setLocation(location);

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


   /* @DeleteMapping("/thumbnail")
    @CacheEvict(value = "thumbnails", key = "#path")
    public void deleteThumbnail(@RequestParam String path) throws IOException {
        fileStorageService.deleteFile(path);
    }*/

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


}
