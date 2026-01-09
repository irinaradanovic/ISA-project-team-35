package com.isa.jutjubic.controller;

import com.isa.jutjubic.dto.CommentDto;
import com.isa.jutjubic.model.Comment;
import com.isa.jutjubic.repository.CommentRepository;
import com.isa.jutjubic.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;
    @Autowired
    private CommentRepository commentRepository;

    @GetMapping("/{videoId}/comments")
    public Page<CommentDto> getComments(@PathVariable Long videoId, Pageable pageable) {
        return commentService.getCommentsByVideoId(videoId, pageable);
    }
    @PostMapping("/{videoId}")
    public ResponseEntity<CommentDto> createComment(
            @PathVariable Long videoId,
            @RequestBody Map<String, String> request) {
        String content = request.get("content");
        CommentDto comment = commentService.createComment(videoId, content);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }
    @GetMapping("/test-cache/{id}")
    public ResponseEntity<?> testL2Cache(@PathVariable Long id) {
        long start1 = System.currentTimeMillis();
        Comment comment1 = commentRepository.findById(id).orElse(null);
        long time1 = System.currentTimeMillis() - start1;

        long start2 = System.currentTimeMillis();
        Comment comment2 = commentRepository.findById(id).orElse(null);
        long time2 = System.currentTimeMillis() - start2;

        return ResponseEntity.ok(Map.of(
                "firstCall_ms", time1,
                "secondCall_ms", time2,
                "cacheWorking", time2 < time1,
                "commentId", comment1 != null ? comment1.getId() : null,
                "commentContent", comment1 != null ? comment1.getContent() : null
        ));
    }



}