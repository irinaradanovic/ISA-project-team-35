package com.isa.jutjubic.security.jwt;

import com.isa.jutjubic.security.service.CustomUserDetailsService;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends GenericFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   CustomUserDetailsService userDetailsService) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;

        // 1. AKO JE OPTIONS, PROSLEDI ODMAH (Preflight fix)
        if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        String header = httpRequest.getHeader("Authorization");

        //System.out.println("Authorization header: " + header);


        if (header != null && header.startsWith("Bearer ")) {

            String token = header.substring(7);
            String email = tokenProvider.getEmailFromToken(token);

            var userDetails =
                    userDetailsService.loadUserByUsername(email);

            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource()
                            .buildDetails(httpRequest)
            );

            SecurityContextHolder.getContext()
                    .setAuthentication(authentication);
        }

        chain.doFilter(request, response);
    }
}
