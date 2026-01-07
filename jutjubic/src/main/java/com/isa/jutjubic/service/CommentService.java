package com.isa.jutjubic.service;

import com.isa.jutjubic.dto.CommentDto;
import com.isa.jutjubic.dto.VideoPostDto;
import com.isa.jutjubic.model.Comment;
import com.isa.jutjubic.model.User;
import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.repository.CommentRepository;
import com.isa.jutjubic.repository.UserRepository;
import com.isa.jutjubic.repository.VideoPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private VideoPostRepository videoPostRepository;

    @Autowired
    private UserRepository userRepository;

    // TEMPORARY - fixed user until login is implemented
    private User getCurrentUser() {
        return userRepository.findById(1L).orElseThrow(() ->
                new RuntimeException("User not found"));
    }

    @Transactional
    public List<CommentDto> getCommentsByVideoId(Integer videoId) {
        VideoPost video = videoPostRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        return commentRepository.findByVideoPost(video)
                .stream()
                .sorted((a,b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::mapToDTO)
                .toList();
    }

    private CommentDto mapToDTO(Comment c) {
        return CommentDto.builder()
                .id(c.getId())
                .content(c.getContent())
                .authorUsername(c.getAuthor().getUsername())
                .createdAt(c.getCreatedAt())
                .build();
    }

}