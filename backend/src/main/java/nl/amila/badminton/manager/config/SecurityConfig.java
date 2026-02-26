package nl.amila.badminton.manager.config;

import nl.amila.badminton.manager.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Password encoder bean using BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager bean for authentication
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, CustomUserDetailsService userDetailsService, PasswordEncoder passwordEncoder) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
            .userDetailsService(userDetailsService)
            .passwordEncoder(passwordEncoder);
        return authenticationManagerBuilder.build();
    }

    /**
     * CORS configuration to allow requests from frontend
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PATCH", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Security filter chain for role-based access control
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, AuthenticationManager authenticationManager) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Auth endpoints - public
                .requestMatchers("/api/auth/**").permitAll()

                // Tournament endpoints - role-based access
                .requestMatchers(HttpMethod.POST, "/api/tournaments").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/tournaments/admins/available").hasRole("ADMIN")
                .requestMatchers("/api/tournaments/*/admins/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/tournaments/*/toggle").hasRole("ADMIN")
                .requestMatchers("/api/tournaments/*/players/**").hasAnyRole("ADMIN", "TOURNY_ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/tournaments/*/settings").hasAnyRole("ADMIN", "TOURNY_ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/tournaments/*/rankings").permitAll()

                // Player-only tournament endpoints (must be before the broad admin GET matcher)
                .requestMatchers(HttpMethod.GET, "/api/tournaments/player-list").hasRole("PLAYER")
                .requestMatchers(HttpMethod.GET, "/api/tournaments/*/player-view").hasRole("PLAYER")

                // Player-only game-day endpoints
                .requestMatchers(HttpMethod.GET, "/api/tournaments/*/game-days/player-list").hasRole("PLAYER")
                .requestMatchers(HttpMethod.GET, "/api/tournaments/*/game-days/*/player-view").hasRole("PLAYER")
                .requestMatchers(HttpMethod.PUT, "/api/tournaments/*/game-days/*/groups/*/matches/*/player-score").hasRole("PLAYER")

                // Admin/TournyAdmin game-day and tournament GET endpoints
                .requestMatchers(HttpMethod.GET, "/api/tournaments/**").hasAnyRole("ADMIN", "TOURNY_ADMIN")

                // All other requests require authentication
                .anyRequest().authenticated()
            )
            .authenticationManager(authenticationManager)
            .httpBasic(httpBasic -> {});

        return http.build();
    }
}

