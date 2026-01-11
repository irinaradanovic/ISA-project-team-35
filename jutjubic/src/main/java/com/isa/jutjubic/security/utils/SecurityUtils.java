package com.isa.jutjubic.security.utils;

import com.isa.jutjubic.security.model.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    //OVDE DOBAVLJAMO ID ULOGOVANOG KORISNIKA
    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        return principal.getId();
    }
}
