package com.isa.jutjubic.controller;

import com.isa.jutjubic.service.LikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/videoPosts")
public class LikeController {

    @Autowired
    private LikeService likeService;

    @PostMapping("/{videoId}/like")
    public int toggleLike(@PathVariable Integer videoId) {
        return likeService.toggleLike(videoId);
    }

    @GetMapping("/{videoId}/liked")
    public boolean isLiked(@PathVariable Integer videoId) {
        return likeService.isLiked(videoId);
    }
}
