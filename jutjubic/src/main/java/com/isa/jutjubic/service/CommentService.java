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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Autowired
    private AuthService authService;

    // TEMPORARY - fixed user until login is implemented
    /*/
    private User getCurrentUser() {
        return userRepository.findById(1L).orElseThrow(() ->
                new RuntimeException("User not found"));
    }
*/


    @Transactional(readOnly = true)
    public Page<CommentDto> getCommentsByVideoId(Long videoId, Pageable pageable) {
            Page<Comment> commentPage= commentRepository.findAllByVideoId(videoId,pageable);
            return commentPage.map(this::mapToDTO);
    }

    private CommentDto mapToDTO(Comment c) {
        return CommentDto.builder()
                .id(c.getId())
                .content(c.getContent())
                .authorUsername(c.getAuthor().getUsername())
                .createdAt(c.getCreatedAt())
                .build();
    }

    @Transactional
    public CommentDto createComment(Long videoId, String content) {
        User currentUser = authService.getCurrentUser();

        VideoPost videoPost = videoPostRepository.findById(Math.toIntExact(videoId))
                .orElseThrow(() -> new RuntimeException("Video not found"));

        Comment comment = Comment.builder()
                .content(content)
                .author(currentUser)
                .videoPost(videoPost)
                .createdAt(new Date())
                .build();

        Comment saved = commentRepository.save(comment);
        return mapToDTO(saved);
    }
}