package com.isa.jutjubic.controller;

import com.isa.jutjubic.model.VideoPost;
import com.isa.jutjubic.repository.VideoPostRepository;
import com.isa.jutjubic.service.VideoPostService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test-mq")
public class TestMqController {
    @Autowired
    private VideoPostService videoPostService;
    @Autowired
    private VideoPostRepository repo;

    @GetMapping
    @Transactional
    public String runTest() {
        VideoPost sample = repo.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Baza je prazna"));


        sample.getOwner().getUsername();

        for(int i = 0; i < 50; i++) {
            videoPostService.sendMqComparison(sample, 1024L * (i + 1));
        }
        return "Poslato 50 test poruka u oba formata";
    }
}
