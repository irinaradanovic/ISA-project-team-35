package com.isa.jutjubic.service;

import com.isa.jutjubic.dto.VideoPostDto;
import com.isa.jutjubic.dto.VideoPostUploadDto;
import com.isa.jutjubic.model.User;
import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.repository.UserRepository;
import com.isa.jutjubic.repository.VideoPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
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

    public List<VideoPostDto> getAllPosts() {
        return postRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
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
        dto.setLocation(post.getLocation());
        dto.setOwnerUsername(post.getOwner().getUsername());
        dto.setLikeCount(post.getLikeCount());
        dto.setCommentCount(post.getCommentCount());
        dto.setViewCount(post.getViewCount());
        return dto;
    }

    @Transactional(rollbackFor = Exception.class)
    public VideoPost createPost(VideoPostUploadDto dto) throws IOException {
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

        String videoPath = null;
        String thumbnailPath = null;

        try {
            thumbnailPath = fileStorageService.saveFile(dto.getThumbnail(), "thumbnails");
            videoPath = fileStorageService.saveFile(dto.getVideo(), "videos");

            User owner = userRepository.getById(1L);   //PRIVREMENO POSTAVLJAMO DA VIDEE POSTAVLJA KORISNIK SA ID-EM 1 DOK SE NE URADI LOG IN

            VideoPost post = new VideoPost();
            post.setTitle(dto.getTitle());
            post.setDescription(dto.getDescription());
            post.setTags(dto.getTags());
            post.setThumbnailPath(thumbnailPath);
            post.setVideoPath(videoPath);
            post.setCreatedAt(LocalDateTime.now());
            post.setLocation(dto.getLocation());
            post.setOwner(owner);


            return postRepository.save(post);

        } catch (IOException e) {
            // rollback fajlova ako upload ne uspe
            if (thumbnailPath != null) fileStorageService.deleteFile(thumbnailPath);
            if (videoPath != null) fileStorageService.deleteFile(videoPath);
            throw e;
        }
    }

    @Transactional
    public void deletePost(Integer id) throws IOException {
        VideoPost post = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("VideoPost not found with id " + id));

        // 1. Obrisi fajlove sa diska
        if (post.getThumbnailPath() != null) {
            fileStorageService.deleteFile(post.getThumbnailPath()); // ovo uklanja i iz keša
        }
        if (post.getVideoPath() != null) {
            Path videoPath = Paths.get(post.getVideoPath());
            if (Files.exists(videoPath)) {
                Files.delete(videoPath);
            }
        }

        // 2. Obrisi iz baze
        postRepository.delete(post);
    }


    public VideoPostDto getById(Integer Id){
        VideoPost post =  postRepository.findById(Id).orElseThrow(() -> new NoSuchElementException("VideoPost not found with id " + Id));
        return mapToDto(post);
    }

    @Cacheable(value = "thumbnails", key = "#videoId")
    public byte[] getThumbnail(Integer videoId) {
        VideoPostDto post = getById(videoId);

        System.out.println("Ucitavam thumbnail sa diska");

        try {
            Thread.sleep(2000); // simulacija skupog poziva
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return fileStorageService.loadThumbnail(post.getThumbnailPath());
    }



}
