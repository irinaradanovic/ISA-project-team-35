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
    private final UserTracker userTracker;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   CustomUserDetailsService userDetailsService,
                                   UserTracker userTracker) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.userTracker = userTracker;
    }

    @Override
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String header = httpRequest.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            try {
                String token = header.substring(7);
                String email = tokenProvider.getEmailFromToken(token);

                //Logujemo aktivnost korisnika za Grafanu
                if (email != null) {
                    userTracker.logActivity(email);
                }

                var userDetails = userDetailsService.loadUserByUsername(email);

                var authentication = new UsernamePasswordAuthenticationToken(
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

            } catch(Exception e) {

                System.err.println("Security Filter: Neuspešna autentifikacija (Baza nedostupna ili loš token). Nastavljam kao anoniman korisnik.");
            }
        }
        chain.doFilter(request, response);
    }
}