package com.isa.jutjubic.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class TranscodingPresetRegistry {

    private final Map<String, TranscodingPreset> presets = Map.of(
            "H264_720P", new TranscodingPreset(
                    "H264_720P",
                    List.of(
                            "-c:v", "libx264",
                            "-preset", "veryfast",
                            "-crf", "23",
                            "-vf", "scale=-2:720",
                            "-c:a", "aac",
                            "-b:a", "128k",
                            "-movflags", "+faststart"
                    )
            )
    );

    @Value("${transcoding.preset.default:H264_720P}")
    private String defaultPreset;

    public TranscodingPreset getDefaultPreset() {
        return presets.get(defaultPreset);
    }
}