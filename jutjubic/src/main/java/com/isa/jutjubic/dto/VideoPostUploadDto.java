package com.isa.jutjubic.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

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

    private String city;
    private String country;
    private Double latitude;
    private Double longitude;

    private String address;  //ovo korisnik unosi za lokaciju

    private LocalDateTime scheduledAt; // novo polje za zakazani prikaz

}
