package com.isa.jutjubic.dto;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadEventDto {
    private Long id;
    private String title;
    private String author;
    private Long sizeBytes;
}
