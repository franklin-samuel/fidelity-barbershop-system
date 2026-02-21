package app.system.fidelity.security.configuration;

import app.system.fidelity.security.filter.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs",
                                "/actuator/**"
                        ).permitAll()

                        // User
                        .requestMatchers("/api/user/**").hasAuthority("ADMIN")

                        // Settings
                        .requestMatchers("/api/settings/**").hasAuthority("ADMIN")

                        // Appointment
                        .requestMatchers(HttpMethod.GET, "/api/appointment/**").hasAnyAuthority("ADMIN", "BARBER")
                        .requestMatchers(HttpMethod.POST, "/api/appointment/**").hasAnyAuthority("ADMIN", "BARBER")
                        .requestMatchers(HttpMethod.PATCH, "/api/appointment/**").hasAnyAuthority("ADMIN", "BARBER")

                        // Barber
                        .requestMatchers(HttpMethod.POST, "/api/barber/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/barber/**").hasAuthority("ADMIN")

                        // Service
                        .requestMatchers(HttpMethod.GET, "/api/service/**").hasAnyAuthority("ADMIN", "BARBER")
                        .requestMatchers(HttpMethod.POST, "/api/service/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/service/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/service/**").hasAuthority("ADMIN")

                        // Product
                        .requestMatchers(HttpMethod.GET, "/api/product/**").hasAnyAuthority("ADMIN", "BARBER")
                        .requestMatchers(HttpMethod.POST, "/api/product/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/product/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/product/**").hasAuthority("ADMIN")

                        // Customer
                        .requestMatchers("/api/customer/**").hasAnyAuthority("ADMIN", "BARBER")

                        // Appointment
                        .requestMatchers(HttpMethod.POST, "/api/appointment/**").hasAnyAuthority("ADMIN", "BARBER")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}