package com.isa.jutjubic.controller;

import com.isa.jutjubic.dto.CommentDto;
import com.isa.jutjubic.model.Comment;
import com.isa.jutjubic.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/videoPosts")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @GetMapping("/{videoId}/comments")
    public List<CommentDto> getComments(@PathVariable Integer videoId) {
        return commentService.getCommentsByVideoId(videoId);
    }


}