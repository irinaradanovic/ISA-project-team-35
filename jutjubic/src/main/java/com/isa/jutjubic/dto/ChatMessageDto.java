package com.isa.jutjubic.dto;

import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@ToString @Builder
public class ChatMessageDto {   // ne cuvamo istoriju chata tokom streaminga, pa je dto dovoljan

    private Integer videoId;
    private String sender;
    private String content;
    private LocalDateTime timestamp;

}
