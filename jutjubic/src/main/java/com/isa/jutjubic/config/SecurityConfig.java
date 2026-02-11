package com.isa.jutjubic.config;

import com.isa.jutjubic.security.jwt.JwtAuthenticationFilter;
import com.isa.jutjubic.security.jwt.JwtTokenProvider;
import com.isa.jutjubic.security.service.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;

   @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                //.cors(Customizer.withDefaults()) // Aktivira CORS konfiguraciju unutar Spring Security-a
                .cors(cors -> cors.disable()) // Isključi Springov CORS jer Nginx to radi
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // JWT je stateless
                .authorizeHttpRequests(auth -> auth

                        // 2. DOZVOLI SVE OPTIONS ZAHTEVE (Preflight)
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        .requestMatchers("/api/auth/**").permitAll() // registracija i login dostupni svima
                        .requestMatchers("/uploads/**").permitAll() // DOZVOLI PRISTUP ENDPOINTU ZA THUMBNAIL I VIDEE
                        .requestMatchers("/api/").permitAll()  //dozvoli home
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/videoPosts/**").permitAll() //dozvoli pregled vide
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/comments/**").permitAll() //dozvoli pregled komentara
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/videoPosts/*/thumbnail").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/videoPosts").permitAll() // Dozvoli listanje videa svima
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users/current-user").authenticated()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/users/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/map/**").permitAll() //dozvoli mapu
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/map/**").permitAll() // POST map tiles
                        .requestMatchers("/socket/**").permitAll()  // omogucava web socket handshake
                        .requestMatchers("/api/cluster/**").permitAll() // Dozvoli  custom health check
                        .requestMatchers("/actuator/**").permitAll()    // Dozvoli Spring Actuator
                        .anyRequest().authenticated() // sve ostalo zahteva autentifikaciju
                )
                .addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }


    /*@Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }*/

    // Za autentifikaciju u service sloju
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

   /* @Bean
    public WebMvcConfigurer webMvcConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addResourceHandlers(ResourceHandlerRegistry registry) {
                // Mapira URL /uploads/** na fizičku lokaciju na disku
                String uploadPath = Paths.get(System.getProperty("user.dir"), "uploads").toUri().toString();

                registry.addResourceHandler("/uploads/**")
                        .addResourceLocations(uploadPath);
            }

            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:5173")  //front
                        .allowedMethods("*")
                        .allowedHeaders("*")
                        .allowCredentials(true);
            }
        };
    }*/

    /*@Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173"));
        configuration.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }*/

}
