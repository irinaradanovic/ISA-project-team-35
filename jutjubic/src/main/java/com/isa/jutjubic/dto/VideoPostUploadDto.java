package com.isa.jutjubic.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class VideoPostUploadDto {
    //za uploading
    private String title;
    private String description;
    private String tags;
    private MultipartFile video;
    private MultipartFile thumbnail;
    //private String location;
    private String city;
    private String country;
    private double latitude;
    private double longitude;

}
