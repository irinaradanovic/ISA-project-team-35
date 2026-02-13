package com.isa.jutjubic.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UploadEventDto {
    private Long id;
    private String title;
    private String author;
    private Long sizeBytes;
}
