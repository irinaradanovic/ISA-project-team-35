package com.isa.jutjubic.service;

import java.util.List;

public record TranscodingPreset(String name, List<String> ffmpegArgs) { }