package com.isa.jutjubic.security.model;

import com.isa.jutjubic.model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class UserPrincipal implements UserDetails {

    private final User user;

    public UserPrincipal(User user) {
        this.user = user;
    }

    public Long getId() {
        return user.getId();
    }

    public String getEmail() {
        return user.getEmail();
    }

    // =====================
    // UserDetails methods
    // =====================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Faza 1: nema uloga
        return Collections.emptyList();
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        // Login je preko email-a
        return user.getEmail();
    }


    public String getActualUsername(){
        return user.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // 🔴 ključna linija: neaktivan korisnik NE može da se uloguje
        return user.isActive();
    }
}
