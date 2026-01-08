package com.isa.jutjubic.dto;

import com.isa.jutjubic.model.User;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.*;

import java.util.Date;
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
@Builder
public class CommentDto {
    private Long id;
    private String content;
    private String authorUsername;
    private Date createdAt;
}
