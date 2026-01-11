package com.isa.jutjubic.controller;

import com.isa.jutjubic.dto.UserProfileUpdateDto;
import com.isa.jutjubic.security.utils.SecurityUtils;
import com.isa.jutjubic.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    @Autowired
    private UserService userService;


    @GetMapping("/current-user")
    public ResponseEntity<?> getLoggedInUser(){
        Long id = SecurityUtils.getCurrentUserId();
        try {
            return ResponseEntity.ok(userService.getProfile(id));
        } catch (NoSuchElementException e){
            return ResponseEntity.status(404).body("User not found");
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProfile(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(userService.getProfile(id));
        } catch (NoSuchElementException e){
            return ResponseEntity.status(404).body("User not found");
        }
    }

    @PutMapping("/current-user")
    public ResponseEntity<?> updateProfile(
            @RequestBody UserProfileUpdateDto dto
    ) {
        try {
            return ResponseEntity.ok(userService.updateProfile(dto));
        } catch (NoSuchElementException e){
            return ResponseEntity.status(404).body("User not found");
        }
    }
}
