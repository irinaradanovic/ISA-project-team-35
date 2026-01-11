package com.isa.jutjubic.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileDto {

    private Long id;
    private String email;
    private String username;
    private String firstName;
    private String lastName;
    private String address;
}
