package com.isa.jutjubic.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class TranscodeRequestDto {
    private String messageId;
    private Integer videoPostId;
    private String inputPath;
    private String outputPath;
    private String presetName;
    private List<String> ffmpegArgs;
}
