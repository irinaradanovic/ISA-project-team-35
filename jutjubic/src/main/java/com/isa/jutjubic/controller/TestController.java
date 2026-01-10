package com.isa.jutjubic.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/test/public")
    public String publicEndpoint() {
        return "Hello public";
    }

    @GetMapping("/api/test/private")
    public String privateEndpoint() {
        return "Hello private";
    }
}
